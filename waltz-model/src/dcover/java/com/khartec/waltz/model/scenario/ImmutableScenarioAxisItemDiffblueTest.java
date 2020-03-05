package com.khartec.waltz.model.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.AxisOrientation;
import org.junit.Test;

public class ImmutableScenarioAxisItemDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableScenarioAxisItem.Json actualJson = new ImmutableScenarioAxisItem.Json();

    // Assert
    assertNull(actualJson.domainItem);
    assertEquals(0, actualJson.position);
    assertFalse(actualJson.scenarioIdIsSet);
    assertEquals(0L, actualJson.scenarioId);
    assertNull(actualJson.axisOrientation);
    assertFalse(actualJson.positionIsSet);
  }
  @Test
  public void setAxisOrientationTest() {
    // Arrange
    ImmutableScenarioAxisItem.Json json = new ImmutableScenarioAxisItem.Json();

    // Act
    json.setAxisOrientation(AxisOrientation.ROW);

    // Assert
    assertEquals(AxisOrientation.ROW, json.axisOrientation);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableScenarioAxisItem.Json json = new ImmutableScenarioAxisItem.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertEquals(1, json.position);
    assertTrue(json.positionIsSet);
  }
  @Test
  public void setScenarioIdTest() {
    // Arrange
    ImmutableScenarioAxisItem.Json json = new ImmutableScenarioAxisItem.Json();

    // Act
    json.setScenarioId(123L);

    // Assert
    assertTrue(json.scenarioIdIsSet);
    assertEquals(123L, json.scenarioId);
  }
}

