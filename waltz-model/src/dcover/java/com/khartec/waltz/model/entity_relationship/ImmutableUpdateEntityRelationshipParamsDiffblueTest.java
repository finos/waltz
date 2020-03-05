package com.khartec.waltz.model.entity_relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableUpdateEntityRelationshipParamsDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUpdateEntityRelationshipParams.Json actualJson = new ImmutableUpdateEntityRelationshipParams.Json();

    // Assert
    assertNull(actualJson.relationshipKind);
    assertNull(actualJson.description);
  }



  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableUpdateEntityRelationshipParams.Json json = new ImmutableUpdateEntityRelationshipParams.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setRelationshipKindTest() {
    // Arrange
    ImmutableUpdateEntityRelationshipParams.Json json = new ImmutableUpdateEntityRelationshipParams.Json();

    // Act
    json.setRelationshipKind(RelationshipKind.HAS);

    // Assert
    assertEquals(RelationshipKind.HAS, json.relationshipKind);
  }
}

