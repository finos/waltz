package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableSetAttributeCommandDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSetAttributeCommand.Json actualJson = new ImmutableSetAttributeCommand.Json();

    // Assert
    assertNull(actualJson.value);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.name);
  }



  @Test
  public void setEntityReferenceTest() {
    // Arrange
    ImmutableSetAttributeCommand.Json json = new ImmutableSetAttributeCommand.Json();
    ImmutableEntityReference.Json json1 = new ImmutableEntityReference.Json();

    // Act
    json.setEntityReference(json1);

    // Assert
    assertSame(json1, json.entityReference);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSetAttributeCommand.Json json = new ImmutableSetAttributeCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setValueTest() {
    // Arrange
    ImmutableSetAttributeCommand.Json json = new ImmutableSetAttributeCommand.Json();

    // Act
    json.setValue("value");

    // Assert
    assertEquals("value", json.value);
  }
}

