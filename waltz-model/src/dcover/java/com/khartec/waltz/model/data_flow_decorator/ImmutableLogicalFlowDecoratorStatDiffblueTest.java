package com.khartec.waltz.model.data_flow_decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableLogicalFlowDecoratorStatDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLogicalFlowDecoratorStat.Json actualJson = new ImmutableLogicalFlowDecoratorStat.Json();

    // Assert
    assertFalse(actualJson.dataTypeIdIsSet);
    assertEquals(0, actualJson.totalCount);
    assertNull(actualJson.logicalFlowMeasures);
    assertEquals(0L, actualJson.dataTypeId);
    assertFalse(actualJson.totalCountIsSet);
  }
  @Test
  public void setDataTypeIdTest() {
    // Arrange
    ImmutableLogicalFlowDecoratorStat.Json json = new ImmutableLogicalFlowDecoratorStat.Json();

    // Act
    json.setDataTypeId(123L);

    // Assert
    assertTrue(json.dataTypeIdIsSet);
    assertEquals(123L, json.dataTypeId);
  }
  @Test
  public void setTotalCountTest() {
    // Arrange
    ImmutableLogicalFlowDecoratorStat.Json json = new ImmutableLogicalFlowDecoratorStat.Json();

    // Act
    json.setTotalCount(3);

    // Assert
    assertEquals(3, json.totalCount);
    assertTrue(json.totalCountIsSet);
  }
}

