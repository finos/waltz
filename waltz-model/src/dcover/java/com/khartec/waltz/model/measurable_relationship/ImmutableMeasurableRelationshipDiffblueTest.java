package com.khartec.waltz.model.measurable_relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableMeasurableRelationshipDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurableRelationship.Json actualJson = new ImmutableMeasurableRelationship.Json();

    // Assert
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.relationshipKind);
    assertFalse(actualJson.measurableBIsSet);
    assertNull(actualJson.description);
    assertEquals(0L, actualJson.measurableB);
    assertEquals(0L, actualJson.measurableA);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.measurableAIsSet);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableMeasurableRelationship.Json json = new ImmutableMeasurableRelationship.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableMeasurableRelationship.Json json = new ImmutableMeasurableRelationship.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setMeasurableATest() {
    // Arrange
    ImmutableMeasurableRelationship.Json json = new ImmutableMeasurableRelationship.Json();

    // Act
    json.setMeasurableA(1L);

    // Assert
    assertEquals(1L, json.measurableA);
    assertTrue(json.measurableAIsSet);
  }
  @Test
  public void setMeasurableBTest() {
    // Arrange
    ImmutableMeasurableRelationship.Json json = new ImmutableMeasurableRelationship.Json();

    // Act
    json.setMeasurableB(1L);

    // Assert
    assertTrue(json.measurableBIsSet);
    assertEquals(1L, json.measurableB);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableMeasurableRelationship.Json json = new ImmutableMeasurableRelationship.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setRelationshipKindTest() {
    // Arrange
    ImmutableMeasurableRelationship.Json json = new ImmutableMeasurableRelationship.Json();

    // Act
    json.setRelationshipKind(MeasurableRelationshipKind.WEAKLY_RELATES_TO);

    // Assert
    assertEquals(MeasurableRelationshipKind.WEAKLY_RELATES_TO, json.relationshipKind);
  }
}

