package com.khartec.waltz.integration_test.inmem.helpers;

import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.integration_test.inmem.helpers.NameHelper.mkName;

@Service
public class PhysicalFlowHelper {

    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecificationService physicalSpecificationService;

    @Autowired
    public PhysicalFlowHelper(PhysicalFlowService physicalFlowService,
                              PhysicalSpecificationService physicalSpecificationService) {
        this.physicalFlowService = physicalFlowService;
        this.physicalSpecificationService = physicalSpecificationService;
    }

    public PhysicalFlowCreateCommandResponse createPhysicalFlow(Long flowId, Long specId, String name) {

        PhysicalSpecification spec = physicalSpecificationService.getById(specId);

        ImmutableFlowAttributes flowAttributes = ImmutableFlowAttributes.builder()
                .transport(TransportKindValue.UNKNOWN)
                .description("")
                .basisOffset(1)
                .criticality(Criticality.MEDIUM)
                .frequency(FrequencyKind.DAILY)
                .build();

        ImmutablePhysicalFlowCreateCommand createCmd = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(flowId)
                .specification(spec)
                .flowAttributes(flowAttributes)
                .build();

        return physicalFlowService.create(createCmd, mkName(name));
    }

}
