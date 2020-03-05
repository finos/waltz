package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutablePhysicalFlowSpecDefinitionChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowSpecDefinitionChangeCommand.Json actualJson = new ImmutablePhysicalFlowSpecDefinitionChangeCommand.Json();

    // Assert
    assertEquals(0L, actualJson.newSpecDefinitionId);
    assertFalse(actualJson.newSpecDefinitionIdIsSet);
  }
  @Test
  public void setNewSpecDefinitionIdTest() {
    // Arrange
    ImmutablePhysicalFlowSpecDefinitionChangeCommand.Json json = new ImmutablePhysicalFlowSpecDefinitionChangeCommand.Json();

    // Act
    json.setNewSpecDefinitionId(123L);

    // Assert
    assertEquals(123L, json.newSpecDefinitionId);
    assertTrue(json.newSpecDefinitionIdIsSet);
  }
}

