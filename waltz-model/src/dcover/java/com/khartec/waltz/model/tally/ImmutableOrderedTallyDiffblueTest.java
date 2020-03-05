package com.khartec.waltz.model.tally;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableOrderedTallyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableOrderedTally.Json<Object> actualJson = new ImmutableOrderedTally.Json<Object>();

    // Assert
    assertFalse(actualJson.indexIsSet);
    assertFalse(actualJson.countIsSet);
    assertNull(actualJson.id);
    assertEquals(0.0, actualJson.count, 0.0);
    assertEquals(0, actualJson.index);
  }
  @Test
  public void setCountTest() {
    // Arrange
    ImmutableOrderedTally.Json<Object> json = new ImmutableOrderedTally.Json<Object>();

    // Act
    json.setCount(10.0);

    // Assert
    assertTrue(json.countIsSet);
    assertEquals(10.0, json.count, 0.0);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableOrderedTally.Json<Object> json = new ImmutableOrderedTally.Json<Object>();

    // Act
    json.setId("123");

    // Assert
    assertTrue(json.id instanceof String);
  }
  @Test
  public void setIndexTest() {
    // Arrange
    ImmutableOrderedTally.Json<Object> json = new ImmutableOrderedTally.Json<Object>();

    // Act
    json.setIndex(1);

    // Assert
    assertTrue(json.indexIsSet);
    assertEquals(1, json.index);
  }
}

