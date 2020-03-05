package com.khartec.waltz.model.cost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableAssetCostDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAssetCost.Json actualJson = new ImmutableAssetCost.Json();

    // Assert
    assertNull(actualJson.assetCode);
    assertNull(actualJson.provenance);
    assertNull(actualJson.cost);
  }



  @Test
  public void setAssetCodeTest() {
    // Arrange
    ImmutableAssetCost.Json json = new ImmutableAssetCost.Json();

    // Act
    json.setAssetCode("assetCode");

    // Assert
    assertEquals("assetCode", json.assetCode);
  }

  @Test
  public void setCostTest() {
    // Arrange
    ImmutableAssetCost.Json json = new ImmutableAssetCost.Json();
    ImmutableCost.Json json1 = new ImmutableCost.Json();

    // Act
    json.setCost(json1);

    // Assert
    assertSame(json1, json.cost);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableAssetCost.Json json = new ImmutableAssetCost.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

