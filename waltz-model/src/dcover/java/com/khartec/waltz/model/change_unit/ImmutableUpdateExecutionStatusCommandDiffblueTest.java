package com.khartec.waltz.model.change_unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.command.EntityChangeCommand;
import org.junit.Test;

public class ImmutableUpdateExecutionStatusCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUpdateExecutionStatusCommand.Json actualJson = new ImmutableUpdateExecutionStatusCommand.Json();

    // Assert
    assertEquals(0L, actualJson.id);
    assertNull(actualJson.executionStatus);
    assertFalse(actualJson.idIsSet);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableUpdateExecutionStatusCommand.Json json = new ImmutableUpdateExecutionStatusCommand.Json();

    // Act
    json.setId(123L);

    // Assert
    assertEquals(123L, json.id);
    assertTrue(json.idIsSet);
  }
}

