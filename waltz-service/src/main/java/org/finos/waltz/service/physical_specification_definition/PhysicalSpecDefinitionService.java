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

package org.finos.waltz.service.physical_specification_definition;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.data.physical_specification_definition.PhysicalSpecDefinitionDao;
import org.finos.waltz.data.physical_specification_definition.PhysicalSpecDefinitionFieldDao;
import org.finos.waltz.data.physical_specification_definition.PhysicalSpecDefinitionSampleFileDao;
import org.finos.waltz.data.physical_specification_definition.PhysicalSpecDefnIdSelectorFactory;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinition;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinition;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinitionChangeCommand;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.ReleaseLifecycleStatus.*;

@Service
public class PhysicalSpecDefinitionService {

    private final ChangeLogService changeLogService;

    private final PhysicalSpecDefinitionDao physicalSpecDefinitionDao;
    private final PhysicalSpecDefinitionFieldDao physicalSpecDefinitionFieldDao;
    private final PhysicalSpecDefinitionSampleFileDao physicalSpecDefinitionSampleFileDao;
    private final Map<ReleaseLifecycleStatus, List<ReleaseLifecycleStatus>> stateTransitions;
    private final PhysicalSpecDefnIdSelectorFactory physicalSpecDefnIdSelectorFactory = new PhysicalSpecDefnIdSelectorFactory();


    @Autowired
    public PhysicalSpecDefinitionService(ChangeLogService changeLogService,
                                         PhysicalSpecDefinitionDao physicalSpecDefinitionDao,
                                         PhysicalSpecDefinitionFieldDao physicalSpecDefinitionFieldDao,
                                         PhysicalSpecDefinitionSampleFileDao physicalSpecDefinitionSampleFileDao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(physicalSpecDefinitionDao, "physicalSpecDefinitionDao cannot be null");
        checkNotNull(physicalSpecDefinitionFieldDao, "physicalSpecDefinitionFieldDao cannot be null");
        checkNotNull(physicalSpecDefinitionSampleFileDao, "physicalSpecDefinitionSampleFileDao cannot be null");

        this.changeLogService = changeLogService;
        this.physicalSpecDefinitionDao = physicalSpecDefinitionDao;
        this.physicalSpecDefinitionFieldDao = physicalSpecDefinitionFieldDao;
        this.physicalSpecDefinitionSampleFileDao = physicalSpecDefinitionSampleFileDao;

        // initialise valid state transitions
        // Hashmap as follows: <currenctState, List of valid transition states>
        stateTransitions = new HashMap<>();
        stateTransitions.put(DRAFT, newArrayList(ACTIVE));
        stateTransitions.put(ACTIVE, newArrayList(DEPRECATED));
        stateTransitions.put(DEPRECATED, newArrayList(ACTIVE, OBSOLETE));
        stateTransitions.put(OBSOLETE, newArrayList());
    }


    public long create(String userName,
                       long specificationId,
                       PhysicalSpecDefinitionChangeCommand command) {

        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        if (command.status() == ACTIVE) {
            physicalSpecDefinitionDao.markExistingActiveAsDeprecated(specificationId, userName);
        }

        long defId = physicalSpecDefinitionDao.create(
                ImmutablePhysicalSpecDefinition.builder()
                        .specificationId(specificationId)
                        .version(command.version())
                        .status(command.status())
                        .delimiter(command.delimiter())
                        .type(command.type())
                        .provenance("waltz")
                        .createdBy(userName)
                        .lastUpdatedBy(userName)
                        .build());

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.ADD)
                        .userId(userName)
                        .parentReference(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specificationId))
                        .message("Spec Definition Version: " + command.version() + " added")
                        .build());

        return defId;

    }


    public int delete(String userName, long specDefinitionId) {

        checkNotNull(userName, "userName cannot be null");

        PhysicalSpecDefinition specDefinition = physicalSpecDefinitionDao.getById(specDefinitionId);

        checkNotNull(specDefinition, "specDefinition cannot be null");

        int defDelCount = physicalSpecDefinitionDao.delete(specDefinitionId);
        int fieldDelCount = physicalSpecDefinitionFieldDao.deleteForSpecDefinition(specDefinitionId);
        int fileDelCount = physicalSpecDefinitionSampleFileDao.deleteForSpecDefinition(specDefinitionId);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.REMOVE)
                        .userId(userName)
                        .parentReference(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specDefinition.specificationId()))
                        .message("Spec Definition Id: " + specDefinitionId + " removed")
                        .build());

        return defDelCount + fieldDelCount + fileDelCount;
    }


    public List<PhysicalSpecDefinition> findForSpecification(long specificationId) {
        return physicalSpecDefinitionDao.findForSpecification(specificationId);
    }


    public List<PhysicalSpecDefinition> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = physicalSpecDefnIdSelectorFactory.apply(options);
        return physicalSpecDefinitionDao.findBySelector(selector);
    }


    public boolean updateStatus(String userName, long specDefinitionId, ReleaseLifecycleStatusChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        PhysicalSpecDefinition specDefinition = physicalSpecDefinitionDao.getById(specDefinitionId);

        checkNotNull(specDefinition, "specDefinition cannot be null");

        ensureNewStatusIsValid(specDefinition, command.newStatus());

        if (command.newStatus() == ACTIVE) {
            physicalSpecDefinitionDao.markExistingActiveAsDeprecated(specDefinition.specificationId(), userName);
        }

        int result = physicalSpecDefinitionDao.updateStatus(specDefinitionId, command.newStatus(), userName);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(userName)
                        .parentReference(mkRef(EntityKind.PHYSICAL_SPECIFICATION, specDefinition.specificationId()))
                        .message("Spec Definition Id: " + specDefinitionId
                                + " status changed to " + command.newStatus())
                        .build());

        return result == 1;
    }


    private void ensureNewStatusIsValid(PhysicalSpecDefinition specDefinition,
                                        ReleaseLifecycleStatus newStatus) {
        ReleaseLifecycleStatus currentStatus = specDefinition.status();

        List<ReleaseLifecycleStatus> validTransitionStates = stateTransitions.get(currentStatus);
        if(validTransitionStates == null || !validTransitionStates.contains(newStatus)) {
            throw new IllegalStateException(String.format("Transition from %s to %s is not valid", currentStatus, newStatus));
        }
    }

}
