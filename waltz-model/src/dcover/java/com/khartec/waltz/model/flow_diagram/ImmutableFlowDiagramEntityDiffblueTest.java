package com.khartec.waltz.model.flow_diagram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableFlowDiagramEntityDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableFlowDiagramEntity.Json actualJson = new ImmutableFlowDiagramEntity.Json();

    // Assert
    assertFalse(actualJson.isNotable);
    assertNull(actualJson.entityReference);
    assertFalse(actualJson.isNotableIsSet);
  }
  @Test
  public void setIsNotableTest() {
    // Arrange
    ImmutableFlowDiagramEntity.Json json = new ImmutableFlowDiagramEntity.Json();

    // Act
    json.setIsNotable(true);

    // Assert
    assertTrue(json.isNotable);
    assertTrue(json.isNotableIsSet);
  }
}

