package com.khartec.waltz.model.tally;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableTallyDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableTally.Json<Object> actualJson = new ImmutableTally.Json<Object>();

    // Assert
    assertFalse(actualJson.countIsSet);
    assertNull(actualJson.id);
    assertEquals(0.0, actualJson.count, 0.0);
  }

  @Test
  public void setCountTest() {
    // Arrange
    ImmutableTally.Json<Object> json = new ImmutableTally.Json<Object>();

    // Act
    json.setCount(10.0);

    // Assert
    assertTrue(json.countIsSet);
    assertEquals(10.0, json.count, 0.0);
  }

  @Test
  public void setIdTest() {
    // Arrange
    ImmutableTally.Json<Object> json = new ImmutableTally.Json<Object>();

    // Act
    json.setId("123");

    // Assert
    assertTrue(json.id instanceof String);
  }
}

