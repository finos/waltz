package com.khartec.waltz.model.measurable_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableRemoveMeasurableRatingCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableRemoveMeasurableRatingCommand.Json actualJson = new ImmutableRemoveMeasurableRatingCommand.Json();

    // Assert
    assertFalse(actualJson.measurableIdIsSet);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.lastUpdate);
    assertEquals(0L, actualJson.measurableId);
  }
  @Test
  public void setMeasurableIdTest() {
    // Arrange
    ImmutableRemoveMeasurableRatingCommand.Json json = new ImmutableRemoveMeasurableRatingCommand.Json();

    // Act
    json.setMeasurableId(123L);

    // Assert
    assertTrue(json.measurableIdIsSet);
    assertEquals(123L, json.measurableId);
  }
}

