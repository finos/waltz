package com.khartec.waltz.model.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.junit.Test;

public class ImmutableScenarioRatingItemDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableScenarioRatingItem.Json actualJson = new ImmutableScenarioRatingItem.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.row);
    assertNull(actualJson.column);
    assertEquals('\u0000', actualJson.rating);
    assertNull(actualJson.description);
    assertFalse(actualJson.ratingIsSet);
    assertFalse(actualJson.scenarioIdIsSet);
    assertEquals(0L, actualJson.scenarioId);
    assertNull(actualJson.item);
    assertNull(actualJson.lastUpdatedBy);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableScenarioRatingItem.Json json = new ImmutableScenarioRatingItem.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableScenarioRatingItem.Json json = new ImmutableScenarioRatingItem.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableScenarioRatingItem.Json json = new ImmutableScenarioRatingItem.Json();

    // Act
    json.setRating('A');

    // Assert
    assertEquals('A', json.rating);
    assertTrue(json.ratingIsSet);
  }
  @Test
  public void setScenarioIdTest() {
    // Arrange
    ImmutableScenarioRatingItem.Json json = new ImmutableScenarioRatingItem.Json();

    // Act
    json.setScenarioId(123L);

    // Assert
    assertTrue(json.scenarioIdIsSet);
    assertEquals(123L, json.scenarioId);
  }
}

