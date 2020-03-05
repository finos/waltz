package com.khartec.waltz.model.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableActorCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableActorCreateCommand.Json actualJson = new ImmutableActorCreateCommand.Json();

    // Assert
    assertFalse(actualJson.isExternalIsSet);
    assertFalse(actualJson.isExternal);
    assertNull(actualJson.name);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableActorCreateCommand.Json json = new ImmutableActorCreateCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIsExternalTest() {
    // Arrange
    ImmutableActorCreateCommand.Json json = new ImmutableActorCreateCommand.Json();

    // Act
    json.setIsExternal(true);

    // Assert
    assertTrue(json.isExternalIsSet);
    assertTrue(json.isExternal);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableActorCreateCommand.Json json = new ImmutableActorCreateCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

