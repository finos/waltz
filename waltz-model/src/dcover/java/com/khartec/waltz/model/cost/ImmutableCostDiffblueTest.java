package com.khartec.waltz.model.cost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import java.math.BigDecimal;
import org.junit.Test;

public class ImmutableCostDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableCost.Json actualJson = new ImmutableCost.Json();

    // Assert
    assertNull(actualJson.costKind);
    assertFalse(actualJson.yearIsSet);
    assertEquals(0, actualJson.year);
    assertNull(actualJson.kind);
    assertNull(actualJson.amount);
  }
  @Test
  public void setAmountTest() {
    // Arrange
    BigDecimal valueOfResult = BigDecimal.valueOf(1L);
    ImmutableCost.Json json = new ImmutableCost.Json();

    // Act
    json.setAmount(valueOfResult);

    // Assert
    assertSame(valueOfResult, json.amount);
  }
  @Test
  public void setCostKindTest() {
    // Arrange
    ImmutableCost.Json json = new ImmutableCost.Json();

    // Act
    json.setCostKind("costKind");

    // Assert
    assertEquals("costKind", json.costKind);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableCost.Json json = new ImmutableCost.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setYearTest() {
    // Arrange
    ImmutableCost.Json json = new ImmutableCost.Json();

    // Act
    json.setYear(1);

    // Assert
    assertTrue(json.yearIsSet);
    assertEquals(1, json.year);
  }
}

