package com.khartec.waltz.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableEntryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntry.Json<Object, Object> actualJson = new ImmutableEntry.Json<Object, Object>();

    // Assert
    assertNull(actualJson.key);
    assertNull(actualJson.value);
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableEntry.Json<Object, Object> json = new ImmutableEntry.Json<Object, Object>();

    // Act
    json.setKey("key");

    // Assert
    assertTrue(json.key instanceof String);
  }
  @Test
  public void setValueTest() {
    // Arrange
    ImmutableEntry.Json<Object, Object> json = new ImmutableEntry.Json<Object, Object>();

    // Act
    json.setValue("value");

    // Assert
    assertTrue(json.value instanceof String);
  }
}

