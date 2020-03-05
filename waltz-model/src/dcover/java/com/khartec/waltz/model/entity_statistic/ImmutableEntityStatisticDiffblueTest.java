package com.khartec.waltz.model.entity_statistic;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableEntityStatisticDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityStatistic.Json actualJson = new ImmutableEntityStatistic.Json();

    // Assert
    assertNull(actualJson.value);
    assertNull(actualJson.definition);
  }
  @Test
  public void setDefinitionTest() {
    // Arrange
    ImmutableEntityStatistic.Json json = new ImmutableEntityStatistic.Json();
    ImmutableEntityStatisticDefinition.Json json1 = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setDefinition(json1);

    // Assert
    assertSame(json1, json.definition);
  }
  @Test
  public void setValueTest() {
    // Arrange
    ImmutableEntityStatistic.Json json = new ImmutableEntityStatistic.Json();
    ImmutableEntityStatisticValue.Json json1 = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setValue(json1);

    // Assert
    assertSame(json1, json.value);
  }
}

