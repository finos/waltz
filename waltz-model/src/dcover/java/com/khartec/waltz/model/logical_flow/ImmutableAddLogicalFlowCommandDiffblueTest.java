package com.khartec.waltz.model.logical_flow;

import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableAddLogicalFlowCommandDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAddLogicalFlowCommand.Json actualJson = new ImmutableAddLogicalFlowCommand.Json();

    // Assert
    assertNull(actualJson.target);
    assertNull(actualJson.source);
  }

}

