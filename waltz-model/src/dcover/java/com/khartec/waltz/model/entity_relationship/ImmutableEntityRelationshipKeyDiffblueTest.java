package com.khartec.waltz.model.entity_relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableEntityRelationshipKeyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityRelationshipKey.Json actualJson = new ImmutableEntityRelationshipKey.Json();

    // Assert
    assertNull(actualJson.b);
    assertNull(actualJson.relationshipKind);
    assertNull(actualJson.a);
  }
  @Test
  public void setRelationshipKindTest() {
    // Arrange
    ImmutableEntityRelationshipKey.Json json = new ImmutableEntityRelationshipKey.Json();

    // Act
    json.setRelationshipKind(RelationshipKind.HAS);

    // Assert
    assertEquals(RelationshipKind.HAS, json.relationshipKind);
  }
}

