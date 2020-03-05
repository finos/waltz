package com.khartec.waltz.model.entity_enum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityEnumValueDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityEnumValue.Json actualJson = new ImmutableEntityEnumValue.Json();

    // Assert
    assertEquals(0L, actualJson.definitionId);
    assertFalse(actualJson.definitionIdIsSet);
    assertNull(actualJson.provenance);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.enumValueKey);
    assertNull(actualJson.lastUpdatedAt);
  }
  @Test
  public void setDefinitionIdTest() {
    // Arrange
    ImmutableEntityEnumValue.Json json = new ImmutableEntityEnumValue.Json();

    // Act
    json.setDefinitionId(123L);

    // Assert
    assertEquals(123L, json.definitionId);
    assertTrue(json.definitionIdIsSet);
  }
  @Test
  public void setEnumValueKeyTest() {
    // Arrange
    ImmutableEntityEnumValue.Json json = new ImmutableEntityEnumValue.Json();

    // Act
    json.setEnumValueKey("enumValueKey");

    // Assert
    assertEquals("enumValueKey", json.enumValueKey);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableEntityEnumValue.Json json = new ImmutableEntityEnumValue.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityEnumValue.Json json = new ImmutableEntityEnumValue.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

