package com.khartec.waltz.model.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableRoleDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableRole.Json actualJson = new ImmutableRole.Json();

    // Assert
    assertNull(actualJson.description);
    assertFalse(actualJson.isCustomIsSet);
    assertNull(actualJson.key);
    assertFalse(actualJson.isCustom);
    assertNull(actualJson.name);
  }


  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableRole.Json json = new ImmutableRole.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setIsCustomTest() {
    // Arrange
    ImmutableRole.Json json = new ImmutableRole.Json();

    // Act
    json.setIsCustom(true);

    // Assert
    assertTrue(json.isCustomIsSet);
    assertTrue(json.isCustom);
  }

  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableRole.Json json = new ImmutableRole.Json();

    // Act
    json.setKey("key");

    // Assert
    assertEquals("key", json.key);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableRole.Json json = new ImmutableRole.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

