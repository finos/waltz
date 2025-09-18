package org.finos.waltz.model.physical_flow;

import org.finos.waltz.model.EntityReference;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.*;
import static org.junit.jupiter.api.Assertions.*;

public class PhysicalFlowTests {

    @Test
    void check_entityReference_whenNameIsSpecified() {
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

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertTrue(ref.name().isPresent());
        assertEquals("test", ref.name().get(),
                "EntityReference.name() should be 'test' when PhysicalFlow.name() is set to 'test'");
    }

    @Test
    void check_entityReference_whenNameIsEmpty() {
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
                .name("")
                .build();

        EntityReference ref = flow.entityReference();

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertTrue(ref.name().isPresent());
        assertEquals("", ref.name().get(),
                "EntityReference.name() should be '' when PhysicalFlow.name() is set to ''");
    }

    @Test
    void check_entityReference_WhenNameIsNotSpecified() {
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
                // do NOT call .name(), omit it to leave as empty
                .build();

        EntityReference ref = flow.entityReference();

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertFalse(ref.name().isPresent(), "Name should not be present");
    }

    @Test
    void check_entityReference_WhenNameIsNull() {
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

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertFalse(ref.name().isPresent(), "Name should not be present");
    }

    @Test
    void check_entityReference_externalIdIsSpecified() {
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

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertTrue(ref.externalId().isPresent());
        assertEquals("test", ref.externalId().get(),
                "EntityReference.externalId() should be 'test' when PhysicalFlow.externalId() is set to 'test'");
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
                .externalId("")
                .build();

        EntityReference ref = flow.entityReference();

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertTrue(ref.externalId().isPresent());
        assertEquals("", ref.externalId().get(),
                "EntityReference.externalId() should be '' when PhysicalFlow.externalId() is set to ''");
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

        assertEquals(100L, flow.id().get(), "id should = 100 Long");
        assertEquals(200L, flow.logicalFlowId(), "logicalFlowId should = 200 Long");
        assertEquals(300L, flow.specificationId(), "specificationId should = 300 Long");
        assertEquals(FrequencyKindValue.of("DAILY"), flow.frequency(), "frequency should = 'DAILY'");
        assertEquals(CriticalityValue.of("MEDIUM"), flow.criticality(), "criticality should = 'MEDIUM'");
        assertEquals(TransportKindValue.UNKNOWN, flow.transport(), "criticality should = 'UNKNOWN'");
        assertEquals(0, flow.basisOffset(), "basisOffset should = 0");
        assertEquals("test PhysicalFlow", flow.description(), "description should = 'test PhysicalFlow'");

        assertEquals(100,ref.id(),"ID should = 100");

        assertEquals("test PhysicalFlow", ref.description(),
                "EntityReference.description() should be 'test PhysicalFlow' when PhysicalFlow.description() is set to 'test PhysicalFlow'");

        assertFalse(ref.externalId().isPresent(), "externalId should not be present");
    }
}
