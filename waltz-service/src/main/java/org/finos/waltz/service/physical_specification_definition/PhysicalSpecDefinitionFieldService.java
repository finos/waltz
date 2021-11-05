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
import org.finos.waltz.data.physical_specification_definition.PhysicalSpecDefinitionFieldDao;
import org.finos.waltz.data.physical_specification_definition.PhysicalSpecDefnFieldIdSelectorFactory;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.logical_data_element.LogicalDataElementChangeCommand;
import org.finos.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionField;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import org.finos.waltz.model.physical_specification_definition.PhysicalSpecDefinitionFieldChangeCommand;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class PhysicalSpecDefinitionFieldService {

    private final ChangeLogService changeLogService;
    private final PhysicalSpecDefinitionFieldDao dao;
    private final PhysicalSpecDefnFieldIdSelectorFactory idSelectorFactory = new PhysicalSpecDefnFieldIdSelectorFactory();


    @Autowired
    public PhysicalSpecDefinitionFieldService(ChangeLogService changeLogService,
                                              PhysicalSpecDefinitionFieldDao dao) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dao, "dao cannot be null");

        this.changeLogService = changeLogService;
        this.dao = dao;
    }


    public long create(String userName,
                       long specDefinitionId,
                       PhysicalSpecDefinitionFieldChangeCommand command) {

        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        return dao.create(ImmutablePhysicalSpecDefinitionField.builder()
                .specDefinitionId(specDefinitionId)
                .name(command.name())
                .position(command.position())
                .type(command.type())
                .description(command.description())
                .logicalDataElementId(command.logicalDataElementId())
                .lastUpdatedBy(userName)
                .build());
    }


    public int delete(long specDefinitionFieldId) {
        return dao.delete(specDefinitionFieldId);
    }


    public int deleteForSpecDefinition(long specDefinitionId) {
        return dao.deleteForSpecDefinition(specDefinitionId);
    }


    public List<PhysicalSpecDefinitionField> findForSpecDefinition(long specDefinitionId) {
        return dao.findForSpecDefinition(specDefinitionId);
    }


    public List<PhysicalSpecDefinitionField> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return dao.findBySelector(selector);
    }


    public int updateDescription(String username, long fieldId, UpdateDescriptionCommand command) {
        checkNotNull(username, "username cannot be null");
        checkNotNull(command, "command cannot be null");

        String description = command.newDescription();
        checkNotEmpty(description, "description cannot be empty");

        int result = dao.updateDescription(fieldId, description);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(username)
                        .parentReference(EntityReference.mkRef(EntityKind.PHYSICAL_SPEC_DEFN_FIELD, fieldId))
                        .message("Physical Field: description changed to " + description)
                        .build());

        return result;
    }


    public int updateLogicalDataElement(String username, long fieldId, LogicalDataElementChangeCommand command) {
        checkNotNull(username, "username cannot be null");
        checkNotNull(command, "command cannot be null");

        Long logicalElementId = command.newLogicalDataElement().map(ref -> ref.id()).orElse(null);
        int result = dao.updateLogicalDataElement(fieldId, logicalElementId);

        changeLogService.write(
                ImmutableChangeLog.builder()
                        .operation(Operation.UPDATE)
                        .userId(username)
                        .parentReference(EntityReference.mkRef(EntityKind.PHYSICAL_SPEC_DEFN_FIELD, fieldId))
                        .message("Physical Field: logical element changed to " + command.newLogicalDataElement().orElse(null))
                        .build());

        return result;
    }
}
