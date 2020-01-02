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

package com.khartec.waltz.jobs.harness;


import com.khartec.waltz.model.FieldDataType;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.physical_specification_definition.*;
import com.khartec.waltz.model.ImmutableReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionFieldService;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionSampleFileService;
import com.khartec.waltz.service.physical_specification_definition.PhysicalSpecDefinitionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class PhysicalSpecDefinitionHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        PhysicalSpecDefinitionService definitionService = ctx.getBean(PhysicalSpecDefinitionService.class);
        PhysicalSpecDefinitionFieldService fieldService = ctx.getBean(PhysicalSpecDefinitionFieldService.class);
        PhysicalSpecDefinitionSampleFileService sampleFileService = ctx.getBean(PhysicalSpecDefinitionSampleFileService.class);

        long specId = 1;
        String userName = "admin";

        long defId = definitionService.create(
                userName,
                specId,
                ImmutablePhysicalSpecDefinitionChangeCommand.builder()
                        .version("1.0")
                        .status(ReleaseLifecycleStatus.DRAFT)
                        .delimiter(",")
                        .type(PhysicalSpecDefinitionType.DELIMITED)
                        .build());

        System.out.println(definitionService.findForSpecification(specId));

        long field1Id = fieldService.create(userName, defId, ImmutablePhysicalSpecDefinitionFieldChangeCommand.builder()
                .name("Field1")
                .description("First field")
                .position(1)
                .type(FieldDataType.STRING)
                .build());

        System.out.println(fieldService.findForSpecDefinition(defId));

        sampleFileService.create(defId, ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.builder()
                .name("SampleFile_1.txt")
                .fileData("1,2,3\n4,5,6\n7,8,9")
                .build());

        System.out.println(sampleFileService.findForSpecDefinition(defId));

//        definitionService.delete(userName, defId);

        definitionService.updateStatus(userName, defId, ImmutableReleaseLifecycleStatusChangeCommand.builder()
                .newStatus(ReleaseLifecycleStatus.ACTIVE)
                .build());

    }
}
