package org.finos.waltz.test_common.helpers;

import org.finos.waltz.common.DateTimeUtilities;
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
import org.finos.waltz.model.proposed_flow.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.finos.waltz.model.EntityLifecycleStatus.ACTIVE;

@Service
public class ProposedFlowWorkflowHelper {

    public Reason getReason() {
        return ImmutableReason.builder()
                .description("test")
                .ratingId(1)
                .build();
    }

    public EntityReference getOwningEntity() {
        return ImmutableEntityReference.builder()
                .id(ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE))
                .kind(APPLICATION)
                .name("AMG")
                .externalId("60487-1")
                .description("Testing")
                .entityLifecycleStatus(ACTIVE)
                .build();
    }

    public PhysicalSpecification getPhysicalSpecification(EntityReference owningEntity) {
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

    public FlowAttributes getFlowAttributes() {
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

    public Set<Long> getDataTypeIdSet() {
        Set<Long> dataTypeIdSet = new HashSet<>();
        dataTypeIdSet.add(41200L);

        return dataTypeIdSet;
    }

    /**
     * Creates a standard 'CREATE' type ProposedFlowCommand with default attributes
     * for use in tests.
     *
     * @param source The source entity of the proposed flow.
     * @param target The target entity of the proposed flow.
     * @return A fully populated ProposedFlowCommand object.
     */
    public ProposedFlowCommand mkCreateCommand(EntityReference source, EntityReference target) {

        // Use existing helpers if available, or define sensible defaults here.
        Reason reason = getReason(); // Assuming you have a helper for this
        FlowAttributes flowAttributes = getFlowAttributes(); // Assuming you have a helper for this

        // Create a default specification. In a test, we often don't need a real persisted one.
        ImmutablePhysicalSpecification defaultSpec = ImmutablePhysicalSpecification.builder()
                .name("Test Spec")
                .description("A default specification for testing")
                .format(DataFormatKindValue.UNKNOWN)
                .owningEntity(source) // Default to the source as the owner
                .lastUpdatedBy("test_helper@db.com") // Provide a default username
                .lastUpdatedAt(DateTimeUtilities.nowUtc()) // Provide the current
                .build();

        return ImmutableProposedFlowCommand.builder()
                .source(ProposedFlowEntityReference.mkRef(source.kind(), source.id()))
                .target(ProposedFlowEntityReference.mkRef(target.kind(), target.id()))
                .reason(reason)
                .specification(defaultSpec)
                .flowAttributes(flowAttributes)
                .dataTypeIds(Collections.emptySet()) // Default to no data types
                .proposalType(ProposalType.CREATE)
                .build();
    }

}
