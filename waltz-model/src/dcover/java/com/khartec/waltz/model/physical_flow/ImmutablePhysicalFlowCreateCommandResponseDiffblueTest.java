package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.command.CommandOutcome;
import org.junit.Test;

public class ImmutablePhysicalFlowCreateCommandResponseDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowCreateCommandResponse.Json actualJson = new ImmutablePhysicalFlowCreateCommandResponse.Json();

    // Assert
    assertNull(actualJson.outcome);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.originalCommand);
  }

  @Test
  public void setOriginalCommandTest() {
    // Arrange
    ImmutablePhysicalFlowCreateCommandResponse.Json json = new ImmutablePhysicalFlowCreateCommandResponse.Json();
    ImmutablePhysicalFlowCreateCommand.Json json1 = new ImmutablePhysicalFlowCreateCommand.Json();

    // Act
    json.setOriginalCommand(json1);

    // Assert
    assertSame(json1, json.originalCommand);
  }

  @Test
  public void setOutcomeTest() {
    // Arrange
    ImmutablePhysicalFlowCreateCommandResponse.Json json = new ImmutablePhysicalFlowCreateCommandResponse.Json();

    // Act
    json.setOutcome(CommandOutcome.SUCCESS);

    // Assert
    assertEquals(CommandOutcome.SUCCESS, json.outcome);
  }
}

