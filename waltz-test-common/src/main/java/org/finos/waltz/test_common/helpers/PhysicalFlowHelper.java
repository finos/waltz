package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.schema.Tables.PHYSICAL_FLOW;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;

@Service
public class PhysicalFlowHelper {

    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecificationService physicalSpecificationService;
    private final DSLContext dsl;

    @Autowired
    public PhysicalFlowHelper(PhysicalFlowService physicalFlowService,
                              PhysicalSpecificationService physicalSpecificationService,
                              DSLContext dslContext) {
        this.physicalFlowService = physicalFlowService;
        this.physicalSpecificationService = physicalSpecificationService;
        this.dsl = dslContext;
    }

    public PhysicalFlowCreateCommandResponse createPhysicalFlow(Long flowId, Long specId, String name) {

        PhysicalSpecification spec = physicalSpecificationService.getById(specId);

        ImmutableFlowAttributes flowAttributes = ImmutableFlowAttributes.builder()
                .transport(TransportKindValue.UNKNOWN)
                .description("")
                .basisOffset(1)
                .criticality(CriticalityValue.of("MEDIUM"))
                .frequency(FrequencyKindValue.of("DAILY"))
                .build();

        ImmutablePhysicalFlowCreateCommand createCmd = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(flowId)
                .specification(spec)
                .flowAttributes(flowAttributes)
                .build();

        return physicalFlowService.create(createCmd, NameHelper.mkName(name));
    }


    public PhysicalFlowDeleteCommandResponse deletePhysicalFlow(Long flowId) {
        return physicalFlowService.delete(
                ImmutablePhysicalFlowDeleteCommand.builder()
                        .flowId(flowId)
                        .build(),
                NameHelper.mkName("deletingFlow"));
    }

    public void markFlowAsReadOnly(long id) {
        dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.IS_READONLY, true)
                .where(PHYSICAL_FLOW.ID.eq(id))
                .execute();
    }

    public void updateExternalIdOnFlowDirectly(long id, String extId) {
        dsl
                .update(PHYSICAL_FLOW)
                .set(PHYSICAL_FLOW.EXTERNAL_ID, extId)
                .where(PHYSICAL_FLOW.ID.eq(id))
                .execute();
    }
}
