package com.khartec.waltz.model.physical_flow_participant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutablePhysicalFlowParticipantDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowParticipant.Json actualJson = new ImmutablePhysicalFlowParticipant.Json();

    // Assert
    assertNull(actualJson.participant);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.lastUpdatedAt);
    assertFalse(actualJson.physicalFlowIdIsSet);
    assertNull(actualJson.kind);
    assertEquals(0L, actualJson.physicalFlowId);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalFlowParticipant.Json json = new ImmutablePhysicalFlowParticipant.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutablePhysicalFlowParticipant.Json json = new ImmutablePhysicalFlowParticipant.Json();

    // Act
    json.setKind(ParticipationKind.SOURCE);

    // Assert
    assertEquals(ParticipationKind.SOURCE, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutablePhysicalFlowParticipant.Json json = new ImmutablePhysicalFlowParticipant.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setPhysicalFlowIdTest() {
    // Arrange
    ImmutablePhysicalFlowParticipant.Json json = new ImmutablePhysicalFlowParticipant.Json();

    // Act
    json.setPhysicalFlowId(123L);

    // Assert
    assertTrue(json.physicalFlowIdIsSet);
    assertEquals(123L, json.physicalFlowId);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutablePhysicalFlowParticipant.Json json = new ImmutablePhysicalFlowParticipant.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

