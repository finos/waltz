package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableCodedReferenceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableCodedReference.Json actualJson = new ImmutableCodedReference.Json();

    // Assert
    assertNull(actualJson.code);
    assertNull(actualJson.name);
  }
  @Test
  public void setCodeTest() {
    // Arrange
    ImmutableCodedReference.Json json = new ImmutableCodedReference.Json();

    // Act
    json.setCode("code");

    // Assert
    assertEquals("code", json.code);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableCodedReference.Json json = new ImmutableCodedReference.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

