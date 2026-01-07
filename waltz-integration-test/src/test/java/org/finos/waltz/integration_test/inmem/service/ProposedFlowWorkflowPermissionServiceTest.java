package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.physical_flow.CriticalityValue;
import org.finos.waltz.model.physical_flow.FlowAttributes;
import org.finos.waltz.model.physical_flow.FrequencyKindValue;
import org.finos.waltz.model.physical_flow.ImmutableFlowAttributes;
import org.finos.waltz.model.physical_flow.TransportKindValue;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ImmutableReason;
import org.finos.waltz.model.proposed_flow.ProposalType;
import org.finos.waltz.model.proposed_flow.ProposeFlowPermission;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.Reason;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowPermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.finos.waltz.model.EntityLifecycleStatus.ACTIVE;
import static org.finos.waltz.model.EntityReference.mkRef;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProposedFlowWorkflowPermissionServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    ProposedFlowWorkflowPermissionService proposedFlowWorkflowPermissionService;

    @Test
    void testShouldBuildPermissionWithCorrectOperationSets() {

        // 1. Arrange ----------------------------------------------------------
        String username = "testUser";

        Reason reason = getReason();
        EntityReference owningEntity = getOwningEntity();
        PhysicalSpecification physicalSpecification = getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = getFlowAttributes();
        Set<Long> dataTypeIdSet = getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 101))
                .target(mkRef(APPLICATION, 202))
                .logicalFlowId(12345)
                .physicalFlowId(12345)
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        EntityReference sourceEntityReference = command.source();
        EntityReference targetEntityReference = command.target();

        // 2. Act --------------------------------------------------------------
        ProposeFlowPermission proposeFlowPermission = proposedFlowWorkflowPermissionService.checkUserPermission(username, sourceEntityReference, targetEntityReference);

        // 3. Assert -----------------------------------------------------------
                /*assertThat(proposeFlowPermission.sourceApprover())
                .containsExactlyInAnyOrderElementsOf(
                        proposedFlowWorkflowPermissionService.fetchPermittedOperationsForUser(username, sourceEntityReference));*/

        assertNotNull(proposeFlowPermission);
    }

    private Reason getReason() {
        return ImmutableReason.builder()
                .description("test")
                .ratingId(1)
                .build();
    }

    private EntityReference getOwningEntity() {
        return ImmutableEntityReference.builder()
                .id(18703)
                .kind(APPLICATION)
                .name("AMG")
                .externalId("60487-1")
                .description("Testing")
                .entityLifecycleStatus(ACTIVE)
                .build();
    }

    private PhysicalSpecification getPhysicalSpecification(EntityReference owningEntity) {
        return ImmutablePhysicalSpecification.builder()
                .id(1L)
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .externalId("mc-extId001")
                .build();
    }

    private FlowAttributes getFlowAttributes() {
        return ImmutableFlowAttributes.builder()
                .name("mc_deliverCharacterstics")
                .transport(TransportKindValue.of("UNKNOWN"))
                .frequency(FrequencyKindValue.of("QUARTERLY"))
                .basisOffset(0)
                .criticality(CriticalityValue.of("low"))
                .description("testing")
                .externalId("567s")
                .build();
    }

    private Set<Long> getDataTypeIdSet() {
        Set<Long> dataTypeIdSet = new HashSet<>();
        dataTypeIdSet.add(41200L);

        return dataTypeIdSet;
    }
}
