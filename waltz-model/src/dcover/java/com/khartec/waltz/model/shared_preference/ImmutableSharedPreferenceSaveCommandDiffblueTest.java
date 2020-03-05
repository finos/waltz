package com.khartec.waltz.model.shared_preference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSharedPreferenceSaveCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSharedPreferenceSaveCommand.Json actualJson = new ImmutableSharedPreferenceSaveCommand.Json();

    // Assert
    assertNull(actualJson.key);
    assertNull(actualJson.value);
    assertNull(actualJson.category);
  }
  @Test
  public void setCategoryTest() {
    // Arrange
    ImmutableSharedPreferenceSaveCommand.Json json = new ImmutableSharedPreferenceSaveCommand.Json();

    // Act
    json.setCategory("category");

    // Assert
    assertEquals("category", json.category);
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableSharedPreferenceSaveCommand.Json json = new ImmutableSharedPreferenceSaveCommand.Json();

    // Act
    json.setKey("key");

    // Assert
    assertEquals("key", json.key);
  }
  @Test
  public void setValueTest() {
    // Arrange
    ImmutableSharedPreferenceSaveCommand.Json json = new ImmutableSharedPreferenceSaveCommand.Json();

    // Act
    json.setValue("value");

    // Assert
    assertEquals("value", json.value);
  }
}

