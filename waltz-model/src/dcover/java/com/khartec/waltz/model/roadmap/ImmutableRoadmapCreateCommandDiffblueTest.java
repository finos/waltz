package com.khartec.waltz.model.roadmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableRoadmapCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableRoadmapCreateCommand.Json actualJson = new ImmutableRoadmapCreateCommand.Json();

    // Assert
    assertNull(actualJson.name);
    assertNull(actualJson.columnType);
    assertNull(actualJson.rowType);
    assertFalse(actualJson.ratingSchemeIdIsSet);
    assertNull(actualJson.linkedEntity);
    assertEquals(0L, actualJson.ratingSchemeId);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableRoadmapCreateCommand.Json json = new ImmutableRoadmapCreateCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setRatingSchemeIdTest() {
    // Arrange
    ImmutableRoadmapCreateCommand.Json json = new ImmutableRoadmapCreateCommand.Json();

    // Act
    json.setRatingSchemeId(123L);

    // Assert
    assertTrue(json.ratingSchemeIdIsSet);
    assertEquals(123L, json.ratingSchemeId);
  }
}

