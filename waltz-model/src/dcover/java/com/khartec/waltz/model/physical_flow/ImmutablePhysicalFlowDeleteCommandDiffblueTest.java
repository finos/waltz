package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutablePhysicalFlowDeleteCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowDeleteCommand.Json actualJson = new ImmutablePhysicalFlowDeleteCommand.Json();

    // Assert
    assertFalse(actualJson.flowIdIsSet);
    assertEquals(0L, actualJson.flowId);
  }
  @Test
  public void setFlowIdTest() {
    // Arrange
    ImmutablePhysicalFlowDeleteCommand.Json json = new ImmutablePhysicalFlowDeleteCommand.Json();

    // Act
    json.setFlowId(123L);

    // Assert
    assertTrue(json.flowIdIsSet);
    assertEquals(123L, json.flowId);
  }
}

