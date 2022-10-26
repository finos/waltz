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

package org.finos.waltz.service.change_unit.processors;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.attribute_change.AttributeChange;
import org.finos.waltz.model.change_unit.ChangeUnit;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.service.change_unit.AttributeChangeCommandProcessor;
import org.finos.waltz.service.data_type.DataTypeDecoratorService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.SetUtilities.minus;


@Service
public class DataTypeChangeCommandProcessor implements AttributeChangeCommandProcessor {

    private final PhysicalFlowService physicalFlowService;
    private final DataTypeDecoratorService dataTypeDecoratorService;


    @Autowired
    public DataTypeChangeCommandProcessor(PhysicalFlowService physicalFlowService,
                                          DataTypeDecoratorService dataTypeDecoratorService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(dataTypeDecoratorService, "dataTypeDecoratorService cannot be null");

        this.physicalFlowService = physicalFlowService;
        this.dataTypeDecoratorService = dataTypeDecoratorService;
    }


    @Override
    public String supportedAttribute() {
        return "DataType";
    }


    @Override
    public boolean apply(AttributeChange attributeChange,
                         ChangeUnit changeUnit,
                         String userName) {
        doBasicValidation(attributeChange, changeUnit, userName);
        checkTrue(changeUnit.subjectEntity().kind() == EntityKind.PHYSICAL_FLOW,
                "Change Subject should be a Physical Flow");

        // get physical flow
        PhysicalFlow physicalFlow = physicalFlowService.getById(changeUnit.subjectEntity().id());

        // update the specs data types
        Set<Long> oldValues = readValue(attributeChange.oldValue());
        Set<Long> newValues = readValue(attributeChange.newValue());

        EntityReference specificationEntityRef = EntityReference.mkRef(EntityKind.PHYSICAL_SPECIFICATION, physicalFlow.specificationId());
        Set<Long> existing = dataTypeDecoratorService.findByEntityId(specificationEntityRef)
                .stream()
                .map(DataTypeDecorator::dataTypeId)
                .collect(toSet());

        Set<Long> toAdd = minus(newValues, oldValues, existing);
        Set<Long> toRemove = minus(oldValues, newValues);


        int removedCount = dataTypeDecoratorService.removeDataTypeDecorator(userName, specificationEntityRef, toRemove);
        int[] addedCount = dataTypeDecoratorService.addDecorators(userName, specificationEntityRef, toAdd);

        return removedCount == toRemove.size() && addedCount.length == toAdd.size();
    }


    private Set<Long> readValue(String val) {
        try {
            List<HashMap> list = getJsonMapper().readValue(val, List.class);
            return list
                    .stream()
                    .map(hm -> hm.get("dataTypeId"))
                    .map(d -> Long.valueOf(d.toString()))
                    .collect(toSet());
        } catch (IOException e) {
            return SetUtilities.asSet();
        }
    }
}
