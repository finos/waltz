package com.khartec.waltz.model.entity_enum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IconProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableEntityEnumDefinitionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityEnumDefinition.Json actualJson = new ImmutableEntityEnumDefinition.Json();

    // Assert
    assertFalse(actualJson.isEditable);
    assertFalse(actualJson.isEditableIsSet);
    assertFalse(actualJson.positionIsSet);
    assertNull(actualJson.enumValueType);
    assertEquals(0, actualJson.position);
    assertNull(actualJson.icon);
    assertNull(actualJson.entityKind);
    assertNull(actualJson.name);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityKindTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.entityKind);
  }
  @Test
  public void setEnumValueTypeTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setEnumValueType("enumValueType");

    // Assert
    assertEquals("enumValueType", json.enumValueType);
  }
  @Test
  public void setIconTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setIcon("icon");

    // Assert
    assertEquals("icon", json.icon);
  }
  @Test
  public void setIsEditableTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setIsEditable(true);

    // Assert
    assertTrue(json.isEditable);
    assertTrue(json.isEditableIsSet);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableEntityEnumDefinition.Json json = new ImmutableEntityEnumDefinition.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertTrue(json.positionIsSet);
    assertEquals(1, json.position);
  }
}

