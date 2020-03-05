package com.khartec.waltz.model.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableFieldChangeDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableFieldChange.Json<Object> actualJson = new ImmutableFieldChange.Json<Object>();

    // Assert
    assertNull(actualJson.description);
    assertNull(actualJson.oldVal);
    assertNull(actualJson.newVal);
  }

  @Test
  public void fromJsonTest() {
    // Arrange and Act
    ImmutableFieldChange<Object> actualFromJsonResult = ImmutableFieldChange
        .<Object>fromJson(new ImmutableFieldChange.Json<Object>());

    // Assert
    assertEquals("FieldChange{newVal=null, oldVal=null," + " description=null}", actualFromJsonResult.toString());
    assertNull(actualFromJsonResult.description());
  }

  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableFieldChange.Json<Object> json = new ImmutableFieldChange.Json<Object>();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setNewValTest() {
    // Arrange
    ImmutableFieldChange.Json<Object> json = new ImmutableFieldChange.Json<Object>();

    // Act
    json.setNewVal("newVal");

    // Assert
    assertTrue(json.newVal instanceof String);
  }

  @Test
  public void setOldValTest() {
    // Arrange
    ImmutableFieldChange.Json<Object> json = new ImmutableFieldChange.Json<Object>();

    // Act
    json.setOldVal("oldVal");

    // Assert
    assertTrue(json.oldVal instanceof String);
  }
}

