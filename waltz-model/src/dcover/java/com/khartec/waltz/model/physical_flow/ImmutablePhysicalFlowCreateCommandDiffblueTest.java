package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutablePhysicalFlowCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowCreateCommand.Json actualJson = new ImmutablePhysicalFlowCreateCommand.Json();

    // Assert
    assertNull(actualJson.specification);
    assertFalse(actualJson.logicalFlowIdIsSet);
    assertEquals(0L, actualJson.logicalFlowId);
    assertNull(actualJson.flowAttributes);
  }
  @Test
  public void setFlowAttributesTest() {
    // Arrange
    ImmutablePhysicalFlowCreateCommand.Json json = new ImmutablePhysicalFlowCreateCommand.Json();
    ImmutableFlowAttributes.Json json1 = new ImmutableFlowAttributes.Json();

    // Act
    json.setFlowAttributes(json1);

    // Assert
    assertSame(json1, json.flowAttributes);
  }
  @Test
  public void setLogicalFlowIdTest() {
    // Arrange
    ImmutablePhysicalFlowCreateCommand.Json json = new ImmutablePhysicalFlowCreateCommand.Json();

    // Act
    json.setLogicalFlowId(123L);

    // Assert
    assertTrue(json.logicalFlowIdIsSet);
    assertEquals(123L, json.logicalFlowId);
  }
}

