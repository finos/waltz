package org.finos.waltz.test_common.helpers;

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
import org.finos.waltz.model.proposed_flow.ImmutableReason;
import org.finos.waltz.model.proposed_flow.Reason;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

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
                .id(18703)
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

}
