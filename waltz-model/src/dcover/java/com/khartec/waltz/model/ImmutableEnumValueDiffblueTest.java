package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableEnumValueDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEnumValue.Json actualJson = new ImmutableEnumValue.Json();

    // Assert
    assertNull(actualJson.type);
    assertEquals(0, actualJson.position);
    assertFalse(actualJson.positionIsSet);
    assertNull(actualJson.description);
    assertNull(actualJson.iconColor);
    assertNull(actualJson.name);
    assertNull(actualJson.icon);
    assertNull(actualJson.key);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIconColorTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setIconColor("iconColor");

    // Assert
    assertEquals("iconColor", json.iconColor);
  }
  @Test
  public void setIconTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setIcon("icon");

    // Assert
    assertEquals("icon", json.icon);
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setKey("key");

    // Assert
    assertEquals("key", json.key);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertEquals(1, json.position);
    assertTrue(json.positionIsSet);
  }
  @Test
  public void setTypeTest() {
    // Arrange
    ImmutableEnumValue.Json json = new ImmutableEnumValue.Json();

    // Act
    json.setType("type");

    // Assert
    assertEquals("type", json.type);
  }
}

