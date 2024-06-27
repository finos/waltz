/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.physical_specification;

import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import org.finos.waltz.data.physical_specification.search.PhysicalSpecificationSearchDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.SetAttributeCommand;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.command.ImmutableCommandResponse;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.model.EntityReference.mkRef;


@Service
public class PhysicalSpecificationService {

    private final ChangeLogService changeLogService;
    private final PhysicalSpecificationDao specificationDao;
    private final PhysicalSpecificationSearchDao specificationSearchDao;
    private final PhysicalSpecificationIdSelectorFactory idSelectorFactory = new PhysicalSpecificationIdSelectorFactory();


    @Autowired
    public PhysicalSpecificationService(ChangeLogService changeLogService,
                                        PhysicalSpecificationDao specificationDao,
                                        PhysicalSpecificationSearchDao specificationSearchDao)
    {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(specificationDao, "specificationDao cannot be null");
        checkNotNull(specificationSearchDao, "specificationSearchDao cannot be null");

        this.changeLogService = changeLogService;
        this.specificationDao = specificationDao;
        this.specificationSearchDao = specificationSearchDao;
    }


    public Set<PhysicalSpecification> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "Entity reference cannot be null");
        return specificationDao.findByEntityReference(ref);
    }


    public PhysicalSpecification getById(long id) {
        return specificationDao.getById(id);
    }


    public Collection<PhysicalSpecification> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return specificationDao.findBySelector(selector);
    }


    public CommandResponse<PhysicalSpecificationDeleteCommand> markRemovedIfUnused(PhysicalSpecificationDeleteCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        CommandOutcome commandOutcome = CommandOutcome.SUCCESS;
        String responseMessage = null;

        PhysicalSpecification specification = specificationDao.getById(command.specificationId());

        if (specification == null) {
            commandOutcome = CommandOutcome.FAILURE;
            responseMessage = "Specification not found";
        } else {
            int deleteCount = specificationDao.markRemovedIfUnused(command.specificationId());

            if (deleteCount == 0) {
                commandOutcome = CommandOutcome.FAILURE;
                responseMessage = "This specification cannot be deleted as it is being referenced by one or more physical flows";
            }
        }

        if (commandOutcome == CommandOutcome.SUCCESS) {
            logChange(username,
                    specification.owningEntity(),
                    String.format("Specification: %s removed",
                            specification.name()),
                    Operation.REMOVE);
        }

        return ImmutableCommandResponse.<PhysicalSpecificationDeleteCommand>builder()
                .entityReference(EntityReference.mkRef(EntityKind.PHYSICAL_SPECIFICATION, command.specificationId()))
                .originalCommand(command)
                .outcome(commandOutcome)
                .message(Optional.ofNullable(responseMessage))
                .build();
    }


    public List<PhysicalSpecification> search(EntitySearchOptions options) {
        checkNotNull(options, "Search options cannot be null");
        return specificationSearchDao.search(options);
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
                .childKind(EntityKind.PHYSICAL_SPECIFICATION)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }


    public Collection<PhysicalSpecification> findByIds(Collection<Long> ids) {
        if(isEmpty(ids)) {
            return emptyList();
        } else{
            return specificationDao.findByIds(ids);
        }
    }

    public Collection<PhysicalSpecification> findByExternalId(String externalId) {
        checkNotNull(externalId, "External id cannot be null");
        return specificationDao.findByExternalId(externalId);
    }

    public int updateExternalId(Long id, String sourceExtId) {
        checkNotNull(id, "Specification id cannot be null");
        checkNotNull(sourceExtId, "External id cannot be null");
        return specificationDao.updateExternalId(id, sourceExtId);
    }

    public boolean isUsed(Long specificationId) {
        checkNotNull(specificationId, "Specification id cannot be null");
        return specificationDao.isUsed(specificationId);
    }

    public Long create(ImmutablePhysicalSpecification specification) {
        return specificationDao.create(specification);
    }

    public int makeActive(Long specificationId, String username) {
        checkNotNull(specificationId, "Specification id cannot be null");
        checkNotNull(username, "Username cannot be null");
        int result = specificationDao.makeActive(specificationId);
        if(result > 0) {
            logChange(username,
                    mkRef(PHYSICAL_SPECIFICATION, specificationId),
                    "Physical Specification has been marked as active",
                    Operation.UPDATE);
        }
        return result;
    }

    public int propagateDataTypesToLogicalFlows(String userName, long id) {
        checkNotNull(userName, "Username cannot be null");

        return specificationDao.propagateDataTypesToLogicalFlows(userName, id);
    }



    public int updateAttribute(String username, SetAttributeCommand command) {

        int rc = doUpdateAttribute(command);

        if (rc != 0) {
            String postamble = format("Updated attribute %s to %s", command.name(), command.value());
            changeLogService.writeChangeLogEntries(
                    command.entityReference(),
                    username,
                    postamble,
                    Operation.UPDATE);
        }

        return rc;
    }


    // -- HELPERS


    private int doUpdateAttribute(SetAttributeCommand command) {
        long flowId = command.entityReference().id();
        switch(command.name()) {
           case "description":
                return specificationDao.updateDescription(flowId, command.value());
            case "format":
                return specificationDao.updateFormat(flowId, DataFormatKindValue.of(command.value()));
            default:
                String errMsg = format(
                        "Cannot update attribute %s on flow as unknown attribute name",
                        command.name());
                throw new UnsupportedOperationException(errMsg);
        }
    }
}
