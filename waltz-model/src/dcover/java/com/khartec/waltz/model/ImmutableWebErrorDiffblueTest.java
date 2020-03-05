package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableWebErrorDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableWebError.Json()).message);
  }


  @Test
  public void setMessageTest() {
    // Arrange
    ImmutableWebError.Json json = new ImmutableWebError.Json();

    // Act
    json.setMessage("message");

    // Assert
    assertEquals("message", json.message);
  }
}

