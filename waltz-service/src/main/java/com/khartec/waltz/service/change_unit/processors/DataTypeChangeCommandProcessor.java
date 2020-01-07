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

package com.khartec.waltz.service.change_unit.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.attribute_change.AttributeChange;
import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.service.change_unit.AttributeChangeCommandProcessor;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.SetUtilities.minus;
import static java.util.stream.Collectors.toSet;


@Service
public class DataTypeChangeCommandProcessor implements AttributeChangeCommandProcessor {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecDataTypeService physicalSpecDataTypeService;


    @Autowired
    public DataTypeChangeCommandProcessor(PhysicalFlowService physicalFlowService,
                                          PhysicalSpecDataTypeService physicalSpecDataTypeService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalSpecDataTypeService, "physicalSpecDataTypeService cannot be null");

        this.physicalFlowService = physicalFlowService;
        this.physicalSpecDataTypeService = physicalSpecDataTypeService;
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

        Set<Long> existing = physicalSpecDataTypeService.findBySpecificationId(physicalFlow.specificationId())
                .stream()
                .map(a -> a.dataTypeId())
                .collect(toSet());

        Set<Long> toAdd = minus(newValues, oldValues, existing);
        Set<Long> toRemove = minus(oldValues, newValues);


        int[] removedCount = physicalSpecDataTypeService.removeDataTypes(userName, physicalFlow.specificationId(), toRemove);
        int[] addedCount = physicalSpecDataTypeService.addDataTypes(userName, physicalFlow.specificationId(), toAdd);

        return removedCount.length == toRemove.size() && addedCount.length == toAdd.size();
    }


    private Set<Long> readValue(String val) {
        try {
            List<HashMap> list = JSON_MAPPER.readValue(val, List.class);
            Set<Long> dataTypeId = list.stream()
                    .map(hm -> hm.get("dataTypeId"))
                    .map(d -> Long.valueOf(d.toString()))
                    .collect(toSet());
            return dataTypeId;
        } catch (IOException e) {
            return SetUtilities.asSet();
        }
    }
}
