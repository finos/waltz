package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.command.CommandOutcome;
import java.util.HashMap;
import org.junit.Test;

public class ImmutablePhysicalFlowUploadCommandResponseDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutablePhysicalFlowUploadCommandResponse.Json()).errors.size());
  }

  @Test
  public void setErrorsTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommandResponse.Json json = new ImmutablePhysicalFlowUploadCommandResponse.Json();
    HashMap<String, String> stringStringMap = new HashMap<String, String>();
    stringStringMap.put("foo", "foo");

    // Act
    json.setErrors(stringStringMap);

    // Assert
    assertSame(stringStringMap, json.errors);
  }

  @Test
  public void setOriginalCommandTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommandResponse.Json json = new ImmutablePhysicalFlowUploadCommandResponse.Json();
    ImmutablePhysicalFlowUploadCommand.Json json1 = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setOriginalCommand(json1);

    // Assert
    assertSame(json1, json.originalCommand);
  }

  @Test
  public void setOutcomeTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommandResponse.Json json = new ImmutablePhysicalFlowUploadCommandResponse.Json();

    // Act
    json.setOutcome(CommandOutcome.SUCCESS);

    // Assert
    assertEquals(CommandOutcome.SUCCESS, json.outcome);
  }

  @Test
  public void setParsedFlowTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommandResponse.Json json = new ImmutablePhysicalFlowUploadCommandResponse.Json();
    ImmutablePhysicalFlowParsed.Json json1 = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setParsedFlow(json1);

    // Assert
    assertSame(json1, json.parsedFlow);
  }
}

