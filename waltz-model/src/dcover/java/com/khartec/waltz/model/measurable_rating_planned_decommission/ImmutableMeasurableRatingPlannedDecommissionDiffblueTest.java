package com.khartec.waltz.model.measurable_rating_planned_decommission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.junit.Test;

public class ImmutableMeasurableRatingPlannedDecommissionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurableRatingPlannedDecommission.Json actualJson = new ImmutableMeasurableRatingPlannedDecommission.Json();

    // Assert
    assertNull(actualJson.id);
    assertNull(actualJson.plannedDecommissionDate);
    assertNull(actualJson.measurableId);
    assertNull(actualJson.createdAt);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.createdBy);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.entityReference);
  }
  @Test
  public void setCreatedByTest() {
    // Arrange
    ImmutableMeasurableRatingPlannedDecommission.Json json = new ImmutableMeasurableRatingPlannedDecommission.Json();

    // Act
    json.setCreatedBy("createdBy");

    // Assert
    assertEquals("createdBy", json.createdBy);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableMeasurableRatingPlannedDecommission.Json json = new ImmutableMeasurableRatingPlannedDecommission.Json();

    // Act
    json.setId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.id);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableMeasurableRatingPlannedDecommission.Json json = new ImmutableMeasurableRatingPlannedDecommission.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setMeasurableIdTest() {
    // Arrange
    ImmutableMeasurableRatingPlannedDecommission.Json json = new ImmutableMeasurableRatingPlannedDecommission.Json();

    // Act
    json.setMeasurableId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.measurableId);
  }
}

