package com.khartec.waltz.model.cost;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableApplicationCostDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableApplicationCost.Json actualJson = new ImmutableApplicationCost.Json();

    // Assert
    assertNull(actualJson.cost);
    assertNull(actualJson.application);
  }
  @Test
  public void setCostTest() {
    // Arrange
    ImmutableApplicationCost.Json json = new ImmutableApplicationCost.Json();
    ImmutableCost.Json json1 = new ImmutableCost.Json();

    // Act
    json.setCost(json1);

    // Assert
    assertSame(json1, json.cost);
  }
}

