package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableEntityReferenceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityReference.Json actualJson = new ImmutableEntityReference.Json();

    // Assert
    assertFalse(actualJson.idIsSet);
    assertNull(actualJson.description);
    assertEquals(0L, actualJson.id);
    assertNull(actualJson.kind);
    assertNull(actualJson.entityLifecycleStatus);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityReference.Json json = new ImmutableEntityReference.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableEntityReference.Json json = new ImmutableEntityReference.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableEntityReference.Json json = new ImmutableEntityReference.Json();

    // Act
    json.setId(123L);

    // Assert
    assertTrue(json.idIsSet);
    assertEquals(123L, json.id);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableEntityReference.Json json = new ImmutableEntityReference.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
}

