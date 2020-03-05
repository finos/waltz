package com.khartec.waltz.model.entity_relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.Operation;
import org.junit.Test;

public class ImmutableEntityRelationshipChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityRelationshipChangeCommand.Json actualJson = new ImmutableEntityRelationshipChangeCommand.Json();

    // Assert
    assertNull(actualJson.entityReference);
    assertNull(actualJson.relationship);
    assertNull(actualJson.operation);
  }
  @Test
  public void setOperationTest() {
    // Arrange
    ImmutableEntityRelationshipChangeCommand.Json json = new ImmutableEntityRelationshipChangeCommand.Json();

    // Act
    json.setOperation(Operation.ADD);

    // Assert
    assertEquals(Operation.ADD, json.operation);
  }
  @Test
  public void setRelationshipTest() {
    // Arrange
    ImmutableEntityRelationshipChangeCommand.Json json = new ImmutableEntityRelationshipChangeCommand.Json();

    // Act
    json.setRelationship(RelationshipKind.HAS);

    // Assert
    assertEquals(RelationshipKind.HAS, json.relationship);
  }
}

