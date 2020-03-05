package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableUserPreferenceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUserPreference.Json actualJson = new ImmutableUserPreference.Json();

    // Assert
    assertNull(actualJson.value);
    assertNull(actualJson.key);
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableUserPreference.Json json = new ImmutableUserPreference.Json();

    // Act
    json.setKey("key");

    // Assert
    assertEquals("key", json.key);
  }
  @Test
  public void setValueTest() {
    // Arrange
    ImmutableUserPreference.Json json = new ImmutableUserPreference.Json();

    // Act
    json.setValue("value");

    // Assert
    assertEquals("value", json.value);
  }
}

