package com.khartec.waltz.model.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.Quarter;
import org.junit.Test;

public class ImmutableCheckpointDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableCheckpoint.Json actualJson = new ImmutableCheckpoint.Json();

    // Assert
    assertEquals(0, actualJson.year);
    assertNull(actualJson.quarter);
    assertNull(actualJson.description);
    assertFalse(actualJson.yearIsSet);
    assertNull(actualJson.name);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableCheckpoint.Json json = new ImmutableCheckpoint.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableCheckpoint.Json json = new ImmutableCheckpoint.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setQuarterTest() {
    // Arrange
    ImmutableCheckpoint.Json json = new ImmutableCheckpoint.Json();

    // Act
    json.setQuarter(Quarter.Q1);

    // Assert
    assertEquals(Quarter.Q1, json.quarter);
  }
  @Test
  public void setYearTest() {
    // Arrange
    ImmutableCheckpoint.Json json = new ImmutableCheckpoint.Json();

    // Act
    json.setYear(1);

    // Assert
    assertEquals(1, json.year);
    assertTrue(json.yearIsSet);
  }
}

