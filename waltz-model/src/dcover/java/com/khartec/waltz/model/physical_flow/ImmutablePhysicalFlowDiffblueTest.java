package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CreatedUserTimestampProvider;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.FreshnessIndicator;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.IsRemovedProvider;
import com.khartec.waltz.model.LastAttestedProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;

public class ImmutablePhysicalFlowDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlow.Json actualJson = new ImmutablePhysicalFlow.Json();

    // Assert
    Optional<LocalDateTime> optional = actualJson.lastAttestedAt;
    assertSame(optional, actualJson.lastAttestedBy);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.specificationDefinitionId);
    assertSame(optional, actualJson.created);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setBasisOffsetTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setBasisOffset(1);

    // Assert
    assertEquals(1, json.basisOffset);
    assertTrue(json.basisOffsetIsSet);
  }
  @Test
  public void setCriticalityTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setCriticality(Criticality.LOW);

    // Assert
    assertEquals(Criticality.LOW, json.criticality);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setFrequencyTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setFrequency(FrequencyKind.ON_DEMAND);

    // Assert
    assertEquals(FrequencyKind.ON_DEMAND, json.frequency);
  }
  @Test
  public void setFreshnessIndicatorTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setFreshnessIndicator(FreshnessIndicator.NEVER_OBSERVED);

    // Assert
    assertEquals(FreshnessIndicator.NEVER_OBSERVED, json.freshnessIndicator);
  }
  @Test
  public void setIsRemovedTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setIsRemoved(true);

    // Assert
    assertTrue(json.isRemoved);
    assertTrue(json.isRemovedIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setLogicalFlowIdTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setLogicalFlowId(123L);

    // Assert
    assertTrue(json.logicalFlowIdIsSet);
    assertEquals(123L, json.logicalFlowId);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setSpecificationIdTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setSpecificationId(123L);

    // Assert
    assertTrue(json.specificationIdIsSet);
    assertEquals(123L, json.specificationId);
  }
  @Test
  public void setTransportTest() {
    // Arrange
    ImmutablePhysicalFlow.Json json = new ImmutablePhysicalFlow.Json();

    // Act
    json.setTransport("transport");

    // Assert
    assertEquals("transport", json.transport);
  }
}

