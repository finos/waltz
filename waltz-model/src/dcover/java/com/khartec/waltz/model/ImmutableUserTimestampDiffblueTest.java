package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableUserTimestampDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUserTimestamp.Json actualJson = new ImmutableUserTimestamp.Json();

    // Assert
    assertNull(actualJson.by);
    assertNull(actualJson.at);
  }


  @Test
  public void setByTest() {
    // Arrange
    ImmutableUserTimestamp.Json json = new ImmutableUserTimestamp.Json();

    // Act
    json.setBy("by");

    // Assert
    assertEquals("by", json.by);
  }
}

