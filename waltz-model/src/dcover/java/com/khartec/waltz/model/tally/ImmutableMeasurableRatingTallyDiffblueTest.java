package com.khartec.waltz.model.tally;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableMeasurableRatingTallyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurableRatingTally.Json actualJson = new ImmutableMeasurableRatingTally.Json();

    // Assert
    assertFalse(actualJson.idIsSet);
    assertFalse(actualJson.countIsSet);
    assertFalse(actualJson.ratingIsSet);
    assertEquals(0L, actualJson.count);
    assertEquals('\u0000', actualJson.rating);
    assertEquals(0L, actualJson.id);
  }
  @Test
  public void setCountTest() {
    // Arrange
    ImmutableMeasurableRatingTally.Json json = new ImmutableMeasurableRatingTally.Json();

    // Act
    json.setCount(3L);

    // Assert
    assertTrue(json.countIsSet);
    assertEquals(3L, json.count);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableMeasurableRatingTally.Json json = new ImmutableMeasurableRatingTally.Json();

    // Act
    json.setId(123L);

    // Assert
    assertTrue(json.idIsSet);
    assertEquals(123L, json.id);
  }
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableMeasurableRatingTally.Json json = new ImmutableMeasurableRatingTally.Json();

    // Act
    json.setRating('A');

    // Assert
    assertTrue(json.ratingIsSet);
    assertEquals('A', json.rating);
  }
}

