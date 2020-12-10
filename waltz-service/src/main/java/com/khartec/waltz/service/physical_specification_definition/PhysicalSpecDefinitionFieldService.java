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

package com.khartec.waltz.service.physical_specification_definition;

import com.khartec.waltz.data.physical_specification_definition.PhysicalSpecDefinitionFieldDao;
import com.khartec.waltz.data.physical_specification_definition.PhysicalSpecDefnFieldIdSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.logical_data_element.LogicalDataElementChangeCommand;
import com.khartec.waltz.model.physical_specification_definition.ImmutablePhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionField;
import com.khartec.waltz.model.physical_specification_definition.PhysicalSpecDefinitionFieldChangeCommand;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;

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
