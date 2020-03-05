package com.khartec.waltz.model.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableCommandResponseDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableCommandResponse.Json<Command> actualJson = new ImmutableCommandResponse.Json<Command>();

    // Assert
    assertNull(actualJson.entityReference);
    assertNull(actualJson.outcome);
    assertNull(actualJson.originalCommand);
  }

  @Test
  public void setOutcomeTest() {
    // Arrange
    ImmutableCommandResponse.Json<Command> json = new ImmutableCommandResponse.Json<Command>();

    // Act
    json.setOutcome(CommandOutcome.SUCCESS);

    // Assert
    assertEquals(CommandOutcome.SUCCESS, json.outcome);
  }
}

