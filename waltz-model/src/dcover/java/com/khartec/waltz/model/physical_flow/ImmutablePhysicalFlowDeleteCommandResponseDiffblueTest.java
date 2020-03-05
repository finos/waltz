package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.command.CommandOutcome;
import org.junit.Test;

public class ImmutablePhysicalFlowDeleteCommandResponseDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowDeleteCommandResponse.Json actualJson = new ImmutablePhysicalFlowDeleteCommandResponse.Json();

    // Assert
    assertFalse(actualJson.isSpecificationUnusedIsSet);
    assertNull(actualJson.originalCommand);
    assertFalse(actualJson.isSpecificationUnused);
    assertFalse(actualJson.isLastPhysicalFlow);
    assertNull(actualJson.outcome);
    assertNull(actualJson.entityReference);
    assertFalse(actualJson.isLastPhysicalFlowIsSet);
  }

  @Test
  public void setIsLastPhysicalFlowTest() {
    // Arrange
    ImmutablePhysicalFlowDeleteCommandResponse.Json json = new ImmutablePhysicalFlowDeleteCommandResponse.Json();

    // Act
    json.setIsLastPhysicalFlow(true);

    // Assert
    assertTrue(json.isLastPhysicalFlow);
    assertTrue(json.isLastPhysicalFlowIsSet);
  }

  @Test
  public void setIsSpecificationUnusedTest() {
    // Arrange
    ImmutablePhysicalFlowDeleteCommandResponse.Json json = new ImmutablePhysicalFlowDeleteCommandResponse.Json();

    // Act
    json.setIsSpecificationUnused(true);

    // Assert
    assertTrue(json.isSpecificationUnusedIsSet);
    assertTrue(json.isSpecificationUnused);
  }

  @Test
  public void setOriginalCommandTest() {
    // Arrange
    ImmutablePhysicalFlowDeleteCommandResponse.Json json = new ImmutablePhysicalFlowDeleteCommandResponse.Json();
    ImmutablePhysicalFlowDeleteCommand.Json json1 = new ImmutablePhysicalFlowDeleteCommand.Json();

    // Act
    json.setOriginalCommand(json1);

    // Assert
    assertSame(json1, json.originalCommand);
  }

  @Test
  public void setOutcomeTest() {
    // Arrange
    ImmutablePhysicalFlowDeleteCommandResponse.Json json = new ImmutablePhysicalFlowDeleteCommandResponse.Json();

    // Act
    json.setOutcome(CommandOutcome.SUCCESS);

    // Assert
    assertEquals(CommandOutcome.SUCCESS, json.outcome);
  }
}

