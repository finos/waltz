package com.khartec.waltz.model.allocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.Operation;
import org.junit.Test;

public class ImmutableMeasurablePercentageChangeDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurablePercentageChange.Json actualJson = new ImmutableMeasurablePercentageChange.Json();

    // Assert
    assertNull(actualJson.measurablePercentage);
    assertNull(actualJson.operation);
  }


  @Test
  public void setMeasurablePercentageTest() {
    // Arrange
    ImmutableMeasurablePercentageChange.Json json = new ImmutableMeasurablePercentageChange.Json();
    ImmutableMeasurablePercentage.Json json1 = new ImmutableMeasurablePercentage.Json();

    // Act
    json.setMeasurablePercentage(json1);

    // Assert
    assertSame(json1, json.measurablePercentage);
  }

  @Test
  public void setOperationTest() {
    // Arrange
    ImmutableMeasurablePercentageChange.Json json = new ImmutableMeasurablePercentageChange.Json();

    // Act
    json.setOperation(Operation.ADD);

    // Assert
    assertEquals(Operation.ADD, json.operation);
  }
}

