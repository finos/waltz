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

package com.khartec.waltz.service.physical_specification;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import com.khartec.waltz.data.physical_specification.search.PhysicalSpecificationSearchDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.model.EntityReference.mkRef;


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
        return specificationDao.findByIds(ids);
    }

    public int updateExternalId(Long id, String sourceExtId) {
        return specificationDao.updateExternalId(id, sourceExtId);
    }

    public boolean isUsed(Long specificationId) {
        return specificationDao.isUsed(specificationId);
    }

    public Long create(ImmutablePhysicalSpecification specification) {
        return specificationDao.create(specification);
    }

    public int makeActive(Long specificationId, String username) {
        int result = specificationDao.makeActive(specificationId);
        if(result > 0) {
            logChange(username,
                    mkRef(PHYSICAL_SPECIFICATION, specificationId),
                    "Physical Specification has been marked as active",
                    Operation.UPDATE);
        }
        return result;
    }
}
