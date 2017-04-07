package com.khartec.waltz.jobs;


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
                .type(PhysicalSpecDefinitionFieldType.STRING)
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
