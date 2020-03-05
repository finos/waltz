package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSimpleDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableSimple.Json()).message);
  }
  @Test
  public void setMessageTest() {
    // Arrange
    ImmutableSimple.Json json = new ImmutableSimple.Json();

    // Act
    json.setMessage("message");

    // Assert
    assertEquals("message", json.message);
  }
}

