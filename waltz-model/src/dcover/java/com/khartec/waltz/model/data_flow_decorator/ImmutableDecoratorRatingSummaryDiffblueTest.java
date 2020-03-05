package com.khartec.waltz.model.data_flow_decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.junit.Test;

public class ImmutableDecoratorRatingSummaryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDecoratorRatingSummary.Json actualJson = new ImmutableDecoratorRatingSummary.Json();

    // Assert
    assertFalse(actualJson.countIsSet);
    assertNull(actualJson.decoratorEntityReference);
    assertEquals(0, actualJson.count);
    assertNull(actualJson.rating);
  }
  @Test
  public void setCountTest() {
    // Arrange
    ImmutableDecoratorRatingSummary.Json json = new ImmutableDecoratorRatingSummary.Json();

    // Act
    json.setCount(3);

    // Assert
    assertTrue(json.countIsSet);
    assertEquals(3, json.count);
  }
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableDecoratorRatingSummary.Json json = new ImmutableDecoratorRatingSummary.Json();

    // Act
    json.setRating(AuthoritativenessRating.PRIMARY);

    // Assert
    assertEquals(AuthoritativenessRating.PRIMARY, json.rating);
  }
}

