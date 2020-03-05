package com.khartec.waltz.model.involvement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.Operation;
import org.junit.Test;

public class ImmutableEntityInvolvementChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityInvolvementChangeCommand.Json actualJson = new ImmutableEntityInvolvementChangeCommand.Json();

    // Assert
    assertNull(actualJson.operation);
    assertNull(actualJson.personEntityRef);
    assertEquals(0, actualJson.involvementKindId);
    assertFalse(actualJson.involvementKindIdIsSet);
  }
  @Test
  public void setInvolvementKindIdTest() {
    // Arrange
    ImmutableEntityInvolvementChangeCommand.Json json = new ImmutableEntityInvolvementChangeCommand.Json();

    // Act
    json.setInvolvementKindId(123);

    // Assert
    assertEquals(123, json.involvementKindId);
    assertTrue(json.involvementKindIdIsSet);
  }
  @Test
  public void setOperationTest() {
    // Arrange
    ImmutableEntityInvolvementChangeCommand.Json json = new ImmutableEntityInvolvementChangeCommand.Json();

    // Act
    json.setOperation(Operation.ADD);

    // Assert
    assertEquals(Operation.ADD, json.operation);
  }
}

