package com.khartec.waltz.model.allocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableMeasurablePercentageDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurablePercentage.Json actualJson = new ImmutableMeasurablePercentage.Json();

    // Assert
    assertEquals(0, actualJson.percentage);
    assertFalse(actualJson.measurableIdIsSet);
    assertFalse(actualJson.percentageIsSet);
    assertEquals(0L, actualJson.measurableId);
  }
  @Test
  public void setMeasurableIdTest() {
    // Arrange
    ImmutableMeasurablePercentage.Json json = new ImmutableMeasurablePercentage.Json();

    // Act
    json.setMeasurableId(123L);

    // Assert
    assertTrue(json.measurableIdIsSet);
    assertEquals(123L, json.measurableId);
  }
  @Test
  public void setPercentageTest() {
    // Arrange
    ImmutableMeasurablePercentage.Json json = new ImmutableMeasurablePercentage.Json();

    // Act
    json.setPercentage(1);

    // Assert
    assertEquals(1, json.percentage);
    assertTrue(json.percentageIsSet);
  }
}

