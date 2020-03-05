package com.khartec.waltz.model.measurable_rating_replacement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.junit.Test;

public class ImmutableMeasurableRatingReplacementDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurableRatingReplacement.Json actualJson = new ImmutableMeasurableRatingReplacement.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.id);
    assertNull(actualJson.decommissionId);
    assertNull(actualJson.createdAt);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.plannedCommissionDate);
    assertNull(actualJson.createdBy);
  }
  @Test
  public void setCreatedByTest() {
    // Arrange
    ImmutableMeasurableRatingReplacement.Json json = new ImmutableMeasurableRatingReplacement.Json();

    // Act
    json.setCreatedBy("createdBy");

    // Assert
    assertEquals("createdBy", json.createdBy);
  }
  @Test
  public void setDecommissionIdTest() {
    // Arrange
    ImmutableMeasurableRatingReplacement.Json json = new ImmutableMeasurableRatingReplacement.Json();

    // Act
    json.setDecommissionId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.decommissionId);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableMeasurableRatingReplacement.Json json = new ImmutableMeasurableRatingReplacement.Json();

    // Act
    json.setId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.id);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableMeasurableRatingReplacement.Json json = new ImmutableMeasurableRatingReplacement.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
}

