package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableLeveledEntityReferenceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLeveledEntityReference.Json actualJson = new ImmutableLeveledEntityReference.Json();

    // Assert
    assertNull(actualJson.entityReference);
    assertFalse(actualJson.levelIsSet);
    assertEquals(0, actualJson.level);
  }
  @Test
  public void setEntityReferenceTest() {
    // Arrange
    ImmutableLeveledEntityReference.Json json = new ImmutableLeveledEntityReference.Json();
    ImmutableEntityReference.Json json1 = new ImmutableEntityReference.Json();

    // Act
    json.setEntityReference(json1);

    // Assert
    assertSame(json1, json.entityReference);
  }
  @Test
  public void setLevelTest() {
    // Arrange
    ImmutableLeveledEntityReference.Json json = new ImmutableLeveledEntityReference.Json();

    // Act
    json.setLevel(1);

    // Assert
    assertTrue(json.levelIsSet);
    assertEquals(1, json.level);
  }
}

