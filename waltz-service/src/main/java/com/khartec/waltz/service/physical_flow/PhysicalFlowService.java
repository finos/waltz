/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.physical_flow;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowSearchDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.attestation.AttestationService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import com.khartec.waltz.service.data_type.DataTypeService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_FLOW;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;


@Service
public class PhysicalFlowService {

    public static final String ATTESTATION_COMMENT = "Creation of physical flow via Waltz";

    private final AttestationService attestationService;
    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final ChangeLogService changeLogService;
    private final LogicalFlowService dataFlowService;
    private final DataFlowDecoratorService dataFlowDecoratorService;
    private final DataTypeService dataTypeService;
    private final SettingsService settingsService;
    private final PhysicalFlowSearchDao searchDao;

    private final static String DEFAULT_DATATYPE_CODE_SETTING_NAME = "settings.data-type.default-code";
    private final static String PROVENANCE = "waltz";

    @Autowired
    public PhysicalFlowService(AttestationService attestationService,
                               ChangeLogService changeLogService,
                               LogicalFlowService dataFlowService,
                               DataFlowDecoratorService dataFlowDecoratorService,
                               DataTypeService dataTypeService,
                               PhysicalFlowDao physicalDataFlowDao,
                               PhysicalSpecificationDao physicalSpecificationDao,
                               SettingsService settingsService,
                               PhysicalFlowSearchDao searchDao) {

        checkNotNull(attestationService, "attestationService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dataFlowService, "dataFlowService cannot be null");
        checkNotNull(dataFlowDecoratorService, "dataFlowDecoratorService cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");
        checkNotNull(physicalDataFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");
        checkNotNull(settingsService, "settingsService cannot be null");
        checkNotNull(searchDao, "searchDao cannot be null");

        this.attestationService = attestationService;
        this.changeLogService = changeLogService;
        this.dataFlowService = dataFlowService;
        this.dataFlowDecoratorService = dataFlowDecoratorService;
        this.dataTypeService = dataTypeService;
        this.physicalFlowDao = physicalDataFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.settingsService = settingsService;
        this.searchDao = searchDao;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByEntityReference(ref);
    }


    public List<PhysicalFlow> findByProducerEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByProducer(ref);
    }


    public List<PhysicalFlow> findByConsumerEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByConsumer(ref);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return physicalFlowDao.findBySpecificationId(specificationId);
    }


    public PhysicalFlow getById(long id) {
        return physicalFlowDao.getById(id);
    }


    public PhysicalFlowDeleteCommandResponse delete(PhysicalFlowDeleteCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        PhysicalFlow flow = physicalFlowDao.getById(command.flowId());
        CommandOutcome commandOutcome = CommandOutcome.SUCCESS;
        String responseMessage = null;
        boolean isSpecificationUnused = false;

        if (flow == null) {
            commandOutcome = CommandOutcome.FAILURE;
            responseMessage = "Flow not found";
        } else {
            int deleteCount = physicalFlowDao.delete(command.flowId());

            if (deleteCount == 0) {
                commandOutcome = CommandOutcome.FAILURE;
                responseMessage = "This flow cannot be deleted as it is being used in a lineage";
            } else {
                isSpecificationUnused = !physicalSpecificationDao.isUsed(flow.specificationId());
            }
        }


        // log changes against source and target entities
        if (commandOutcome == CommandOutcome.SUCCESS) {
            PhysicalSpecification specification = physicalSpecificationDao.getById(flow.specificationId());

            logChange(username,
                    specification.owningEntity(),
                    String.format("Physical flow: %s, from: %s, to: %s removed.",
                            specification.name(),
                            specification.owningEntity().safeName(),
                            flow.target().safeName()),
                    Operation.REMOVE);

            logChange(username,
                    flow.target(),
                    String.format("Physical flow: %s, from: %s removed.",
                            specification.name(),
                            specification.owningEntity().safeName()),
                    Operation.REMOVE);

            logChange(username,
                    specification.owningEntity(),
                    String.format("Physical flow: %s, to: %s removed.",
                            specification.name(),
                            flow.target().safeName()),
                    Operation.REMOVE);

            attestationService.deleteForEntity(EntityReference.mkRef(PHYSICAL_FLOW, command.flowId()), username);
        }


        return ImmutablePhysicalFlowDeleteCommandResponse.builder()
                .originalCommand(command)
                .entityReference(EntityReference.mkRef(PHYSICAL_FLOW, command.flowId()))
                .outcome(commandOutcome)
                .message(Optional.ofNullable(responseMessage))
                .isSpecificationUnused(isSpecificationUnused)
                .build();
    }


    public PhysicalFlowCreateCommandResponse create(PhysicalFlowCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        //check we have a logical data flow
        ensureLogicalDataFlow(command.specification().owningEntity(), command.targetEntity(), username);

        long specId = command
                .specification()
                .id()
                .orElseGet(() -> physicalSpecificationDao.create(command.specification()));

        attestationService.explicitlyAttest(PHYSICAL_SPECIFICATION, specId, username, ATTESTATION_COMMENT);

        PhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .specificationId(specId)
                .basisOffset(command.flowAttributes().basisOffset())
                .frequency(command.flowAttributes().frequency())
                .transport(command.flowAttributes().transport())
                .description(mkSafe(command.flowAttributes().description()))
                .target(command.targetEntity())
                .build();

        long flowId = physicalFlowDao.create(flow);

        logChange(username,
                command.specification().owningEntity(),
                String.format("Added physical flow (%s) to: %s",
                        command.specification().name(),
                        command.targetEntity().safeName()),
                Operation.ADD);

        logChange(username,
                command.targetEntity(),
                String.format("Added physical flow (%s) from: %s",
                        command.specification().name(),
                        command.specification().owningEntity().safeName()),
                Operation.ADD);

        logChange(username,
                EntityReference.mkRef(PHYSICAL_SPECIFICATION, specId),
                String.format("Added physical flow (%s) from: %s, to %s",
                        command.specification().name(),
                        command.specification().owningEntity().safeName(),
                        command.targetEntity().safeName()),
                Operation.ADD);


        attestationService.explicitlyAttest(PHYSICAL_FLOW, flowId, username, ATTESTATION_COMMENT);

        return ImmutablePhysicalFlowCreateCommandResponse.builder()
                .originalCommand(command)
                .outcome(CommandOutcome.SUCCESS)
                .entityReference(EntityReference.mkRef(PHYSICAL_FLOW, flowId))
                .build();
    }


    private void ensureLogicalDataFlow(EntityReference source, EntityReference target, String username) {
        // only ensure logical flow if both entities are applications
        if(!(source.kind().equals(EntityKind.APPLICATION)
                && target.kind().equals(EntityKind.APPLICATION))) {
            return;
        } else {
            LogicalFlow logicalFlow = dataFlowService.findBySourceAndTarget(source, target);
            // attest the flow
            attestationService.explicitlyAttest(LOGICAL_DATA_FLOW, logicalFlow.id().get(), username, ATTESTATION_COMMENT);

            if(logicalFlow == null) {
                Optional<String> defaultDataTypeCode = settingsService.getValue(DEFAULT_DATATYPE_CODE_SETTING_NAME);
                if(!defaultDataTypeCode.isPresent()) {
                    throw new IllegalStateException("No default datatype code  (" + DEFAULT_DATATYPE_CODE_SETTING_NAME + ") in settings table");
                }

                // we need to create a flow with an unknown data type
                ImmutableLogicalFlow newFlow = ImmutableLogicalFlow.builder()
                        .source(source)
                        .target(target)
                        .build();
                logicalFlow = dataFlowService.addFlow(newFlow, username);

                // add decorators
                DataType defaultDataType = dataTypeService.getByCode(defaultDataTypeCode.get());
                EntityReference dataTypeRef = EntityReference.mkRef(
                        EntityKind.DATA_TYPE,
                        defaultDataType.id().get(),
                        defaultDataType.name());
                dataFlowDecoratorService.addDecorators(
                        logicalFlow.id().get(),
                        SetUtilities.fromArray(dataTypeRef),
                        username);
            }
        }
    }


    private void logChange(String userId,
                           EntityReference ref,
                           String message,
                           Operation operation) {

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .childKind(PHYSICAL_FLOW)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }


    public Collection<EntityReference> searchReports(String query) {
        return searchDao.searchReports(query);
    }
}
