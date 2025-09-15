package org.finos.waltz.model.physical_flow;

import org.finos.waltz.model.EntityReference;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PhysicalFlowTests {

    @Test
    void check_entityReference_whenNameIsNotNull() {
        ImmutablePhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .id(100L)
                .logicalFlowId(200L)
                .specificationId(300L)
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .lastUpdatedBy("username")
                .description("test PhysicalFlow")
                .name("test")
                .build();

        EntityReference ref = flow.entityReference();

        // name should = "TEST" as we assigned in builder
        assertEquals("test", ref.name().orElse(""),
                "EntityReference.name() should be 'test' when PhysicalFlow.name() is set to 'test'");
    }

    @Test
    void check_entityReference_shouldUseEmptyStringWhenNameIsNull() {
        ImmutablePhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .id(100L)
                .logicalFlowId(200L)
                .specificationId(300L)
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .lastUpdatedBy("username")
                .description("test PhysicalFlow")
                .name(null)
                .build();

        EntityReference ref = flow.entityReference();

        // name should = "" as we did not assign in builder and should not return NULL
        assertEquals("", ref.name().orElse(""),
                "EntityReference.name() should be '' when PhysicalFlow.name() is null");
    }

    @Test
    void check_entityReference_shouldUseEmptyStringWhenNameIsNotSpecified() {
        ImmutablePhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .id(100L)
                .logicalFlowId(200L)
                .specificationId(300L)
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .lastUpdatedBy("username")
                .description("test PhysicalFlow")
                // do NOT call .externalId(), omit it to leave as empty
                .build();

        EntityReference ref = flow.entityReference();

        // name should = "" as we did not assign in builder and should not return NULL
        assertEquals("", ref.name().orElse(""),
                "EntityReference.name() should be '' when PhysicalFlow.name() is not set");
    }

    @Test
    void check_entityReference_externalIdIsNotNull() {
        ImmutablePhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .id(100L)
                .logicalFlowId(200L)
                .specificationId(300L)
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .lastUpdatedBy("username")
                .description("test PhysicalFlow")
                .externalId("test")
                .build();

        EntityReference ref = flow.entityReference();

        // externalId should = "TEST" as we assigned in builder
        assertEquals("test", ref.externalId().orElse(""),
                "EntityReference.externalId() should be 'test' when PhysicalFlow.externalId() is set  to 'test'");
    }

    @Test
    void check_entityReference_externalIdIsEmpty() {
        ImmutablePhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .id(100L)
                .logicalFlowId(200L)
                .specificationId(300L)
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .lastUpdatedBy("username")
                .description("test PhysicalFlow")
                .externalId(Optional.empty())
                .build();

        EntityReference ref = flow.entityReference();

        // externalId should be empty Optional when PhysicalFlow.externalId is null
        assertFalse(ref.externalId().isPresent(),
                "EntityReference.externalId should be empty when PhysicalFlow.externalId is empty");
    }

    @Test
    void check_entityReference_externalIdIsNotSpecified() {
        ImmutablePhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .id(100L)
                .logicalFlowId(200L)
                .specificationId(300L)
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .lastUpdatedBy("username")
                .description("test PhysicalFlow")
                // do NOT call .externalId(), omit it to leave as empty
                .build();

        EntityReference ref = flow.entityReference();

        // externalId should be empty Optional when PhysicalFlow.externalId is null
        assertFalse(ref.externalId().isPresent(),
                "EntityReference.externalId should be empty when PhysicalFlow.externalId is not specified");
    }
}
