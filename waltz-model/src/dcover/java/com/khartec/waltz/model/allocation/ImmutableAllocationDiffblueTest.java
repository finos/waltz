package com.khartec.waltz.model.allocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableAllocationDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAllocation.Json actualJson = new ImmutableAllocation.Json();

    // Assert
    assertNull(actualJson.lastUpdatedBy);
    assertFalse(actualJson.measurableIdIsSet);
    assertEquals(0L, actualJson.schemeId);
    assertFalse(actualJson.schemeIdIsSet);
    assertNull(actualJson.entityReference);
    assertEquals(0L, actualJson.measurableId);
    assertFalse(actualJson.percentageIsSet);
    assertEquals(0, actualJson.percentage);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.provenance);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableAllocation.Json json = new ImmutableAllocation.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setMeasurableIdTest() {
    // Arrange
    ImmutableAllocation.Json json = new ImmutableAllocation.Json();

    // Act
    json.setMeasurableId(123L);

    // Assert
    assertTrue(json.measurableIdIsSet);
    assertEquals(123L, json.measurableId);
  }
  @Test
  public void setPercentageTest() {
    // Arrange
    ImmutableAllocation.Json json = new ImmutableAllocation.Json();

    // Act
    json.setPercentage(1);

    // Assert
    assertTrue(json.percentageIsSet);
    assertEquals(1, json.percentage);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableAllocation.Json json = new ImmutableAllocation.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setSchemeIdTest() {
    // Arrange
    ImmutableAllocation.Json json = new ImmutableAllocation.Json();

    // Act
    json.setSchemeId(123L);

    // Assert
    assertEquals(123L, json.schemeId);
    assertTrue(json.schemeIdIsSet);
  }
}

