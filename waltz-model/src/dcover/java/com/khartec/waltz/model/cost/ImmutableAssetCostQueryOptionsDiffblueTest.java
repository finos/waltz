package com.khartec.waltz.model.cost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableAssetCostQueryOptionsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAssetCostQueryOptions.Json actualJson = new ImmutableAssetCostQueryOptions.Json();

    // Assert
    assertEquals(0, actualJson.year);
    assertNull(actualJson.idSelectionOptions);
    assertFalse(actualJson.yearIsSet);
  }
  @Test
  public void setYearTest() {
    // Arrange
    ImmutableAssetCostQueryOptions.Json json = new ImmutableAssetCostQueryOptions.Json();

    // Act
    json.setYear(1);

    // Assert
    assertEquals(1, json.year);
    assertTrue(json.yearIsSet);
  }
}

