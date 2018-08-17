/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowSearchDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_FLOW;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.EntityReferenceUtilities.safeName;


@Service
public class PhysicalFlowService {

    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final ChangeLogService changeLogService;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalFlowSearchDao searchDao;
    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory;


    @Autowired
    public PhysicalFlowService(ChangeLogService changeLogService,
                               LogicalFlowService logicalFlowService,
                               PhysicalFlowDao physicalDataFlowDao,
                               PhysicalSpecificationDao physicalSpecificationDao,
                               PhysicalFlowSearchDao searchDao,
                               PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory) {
        this.physicalFlowIdSelectorFactory = physicalFlowIdSelectorFactory;

        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalDataFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");
        checkNotNull(searchDao, "searchDao cannot be null");
        checkNotNull(physicalFlowIdSelectorFactory, "physicalFlowIdSelectorFactory cannot be null");

        this.changeLogService = changeLogService;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowDao = physicalDataFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
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


    public List<PhysicalFlow> findByProducerAndConsumerEntityReferences(EntityReference producer, EntityReference consumer) {
        checkNotNull(producer, "producer cannot be null");
        checkNotNull(consumer, "consumer cannot be null");

        return physicalFlowDao.findByProducerAndConsumer(producer, consumer);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return physicalFlowDao.findBySpecificationId(specificationId);
    }


    public Collection<PhysicalFlow> findByLogicalFlowId(long logicalFlowId) {
        return physicalFlowDao.findByLogicalFlowId(logicalFlowId);
    }


    public Collection<PhysicalFlow> findBySelector(IdSelectionOptions idSelectionOptions) {
        Select<Record1<Long>> selector = physicalFlowIdSelectorFactory.apply(idSelectionOptions);
        return physicalFlowDao.findBySelector(selector);
    }


    public PhysicalFlow getById(long id) {
        return physicalFlowDao.getById(id);
    }


    public PhysicalFlowDeleteCommandResponse delete(PhysicalFlowDeleteCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        PhysicalFlow physicalFlow = physicalFlowDao.getById(command.flowId());
        LogicalFlow logicalFlow = logicalFlowService.getById(physicalFlow.logicalFlowId());

        CommandOutcome commandOutcome = CommandOutcome.SUCCESS;
        String responseMessage = null;
        boolean isSpecificationUnused = false;
        boolean isLastPhysicalFlow = false;

        if (physicalFlow == null) {
            commandOutcome = CommandOutcome.FAILURE;
            responseMessage = "Flow not found";
        } else {
            int deleteCount = physicalFlowDao.delete(command.flowId());

            if (deleteCount == 0) {
                commandOutcome = CommandOutcome.FAILURE;
                responseMessage = "This flow cannot be deleted as it is being used in a lineage";
            } else {
                isSpecificationUnused = !physicalSpecificationDao.isUsed(physicalFlow.specificationId());
                isLastPhysicalFlow = !physicalFlowDao.hasPhysicalFlows(physicalFlow.logicalFlowId());
            }
        }

        // log changes against source and target entities
        if (commandOutcome == CommandOutcome.SUCCESS) {
            PhysicalSpecification specification = physicalSpecificationDao.getById(physicalFlow.specificationId());

            logChange(username,
                    logicalFlow.source(),
                    String.format("Physical flow: %s, from: %s, to: %s removed.",
                            specification.name(),
                            safeName(logicalFlow.source()),
                            safeName(logicalFlow.target())),
                    Operation.REMOVE);

            logChange(username,
                    logicalFlow.target(),
                    String.format("Physical flow: %s, from: %s removed.",
                            specification.name(),
                            safeName(logicalFlow.source())),
                    Operation.REMOVE);

            logChange(username,
                    logicalFlow.source(),
                    String.format("Physical flow: %s, to: %s removed.",
                            specification.name(),
                            safeName(logicalFlow.target())),
                    Operation.REMOVE);
        }


        return ImmutablePhysicalFlowDeleteCommandResponse.builder()
                .originalCommand(command)
                .entityReference(mkRef(PHYSICAL_FLOW, command.flowId()))
                .outcome(commandOutcome)
                .message(Optional.ofNullable(responseMessage))
                .isSpecificationUnused(isSpecificationUnused)
                .isLastPhysicalFlow(isLastPhysicalFlow)
                .build();
    }


    public PhysicalFlowCreateCommandResponse create(PhysicalFlowCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        //check we have a logical data flow
        LogicalFlow logicalFlow = ensureLogicalDataFlowExists(command.logicalFlowId(), username);

        long specId = command
                .specification()
                .id()
                .orElseGet(() -> physicalSpecificationDao.create(ImmutablePhysicalSpecification
                        .copyOf(command.specification())
                        .withLastUpdatedBy(username)
                        .withLastUpdatedAt(nowUtc())));

        PhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .specificationId(specId)
                .basisOffset(command.flowAttributes().basisOffset())
                .frequency(command.flowAttributes().frequency())
                .transport(command.flowAttributes().transport())
                .criticality(command.flowAttributes().criticality())
                .description(mkSafe(command.flowAttributes().description()))
                .logicalFlowId(command.logicalFlowId())
                .lastUpdatedBy(username)
                .lastUpdatedAt(nowUtc())
                .build();

        // ensure existing not in database
        List<PhysicalFlow> byAttributesAndSpecification = physicalFlowDao.findByAttributesAndSpecification(flow);
        if(byAttributesAndSpecification.size() > 0) {
            return ImmutablePhysicalFlowCreateCommandResponse.builder()
                    .originalCommand(command)
                    .outcome(CommandOutcome.FAILURE)
                    .message("Duplicate with existing flow")
                    .entityReference(mkRef(PHYSICAL_FLOW, byAttributesAndSpecification.get(0).id().get()))
                    .build();
        }

        long physicalFlowId = physicalFlowDao.create(flow);

        logChange(username,
                logicalFlow.source(),
                String.format("Added physical flow (%s) to: %s",
                        command.specification().name(),
                        safeName(logicalFlow.target())),
                Operation.ADD);

        logChange(username,
                logicalFlow.target(),
                String.format("Added physical flow (%s) from: %s",
                        command.specification().name(),
                        safeName(logicalFlow.source())),
                Operation.ADD);

        logChange(username,
                mkRef(PHYSICAL_SPECIFICATION, specId),
                String.format("Added physical flow (%s) from: %s, to %s",
                        command.specification().name(),
                        safeName(logicalFlow.source()),
                        safeName(logicalFlow.target())),
                Operation.ADD);

        return ImmutablePhysicalFlowCreateCommandResponse.builder()
                .originalCommand(command)
                .outcome(CommandOutcome.SUCCESS)
                .entityReference(mkRef(PHYSICAL_FLOW, physicalFlowId))
                .build();
    }


    public int updateSpecDefinitionId(String userName, long flowId, PhysicalFlowSpecDefinitionChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        int updateCount = physicalFlowDao.updateSpecDefinition(userName, flowId, command.newSpecDefinitionId());

        if (updateCount > 0) {
            logChange(userName,
                    mkRef(PHYSICAL_FLOW, flowId),
                    String.format("Physical flow id: %d specification definition id changed to: %d",
                            flowId,
                            command.newSpecDefinitionId()),
                    Operation.UPDATE);
        }

        return updateCount;
    }


    public Collection<EntityReference> searchReports(String query) {
        return searchDao.searchReports(query);
    }


    public int cleanupOrphans() {
        return physicalFlowDao.cleanupOrphans();
    }

    private LogicalFlow ensureLogicalDataFlowExists(long logicalFlowId, String username) {
        LogicalFlow logicalFlow = logicalFlowService.getById(logicalFlowId);
        if (logicalFlow == null) {
            throw new IllegalArgumentException("Unknown logical flow: " + logicalFlowId);
        } else {
            if (logicalFlow.entityLifecycleStatus().equals(EntityLifecycleStatus.REMOVED)) {
                logicalFlowService.restoreFlow(logicalFlowId, username);
                return ImmutableLogicalFlow.copyOf(logicalFlow).withEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);
            }
        }
        return logicalFlow;
    }


    public int updateAttribute(String username, SetAttributeCommand command) {

        int rc = doUpdateAttribute(command);

        if (rc != 0) {
            String message = String.format("Updated attribute %s to %s", command.name(), command.value());
            logChange(username, command.entityReference(), message, Operation.UPDATE);
        }

        return rc;
    }


    // -- HELPERS

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


    private int doUpdateAttribute(SetAttributeCommand command) {
        long flowId = command.entityReference().id();
        switch(command.name()) {
            case "criticality":
                return physicalFlowDao.updateCriticality(flowId, Criticality.valueOf(command.value()));
            case "frequency":
                return physicalFlowDao.updateFrequency(flowId, FrequencyKind.valueOf(command.value()));
            case "transport":
                return physicalFlowDao.updateTransport(flowId, TransportKind.valueOf(command.value()));
            case "basisOffset":
                return physicalFlowDao.updateBasisOffset(flowId, Integer.parseInt(command.value()));
            case "description":
                return physicalFlowDao.updateDescription(flowId, command.value());
            default:
                String errMsg = String.format(
                        "Cannot update attribute %s on flow as unknown attribute name",
                        command.name());
                throw new UnsupportedOperationException(errMsg);
        }
    }
}
