package com.khartec.waltz.model.entity_statistic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityStatisticValueDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityStatisticValue.Json actualJson = new ImmutableEntityStatisticValue.Json();

    // Assert
    assertNull(actualJson.reason);
    assertFalse(actualJson.statisticIdIsSet);
    assertNull(actualJson.state);
    assertFalse(actualJson.currentIsSet);
    assertNull(actualJson.createdAt);
    assertEquals(0L, actualJson.statisticId);
    assertNull(actualJson.outcome);
    assertNull(actualJson.entity);
    assertNull(actualJson.value);
    assertFalse(actualJson.current);
    assertNull(actualJson.provenance);
  }




  @Test
  public void setCurrentTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setCurrent(true);

    // Assert
    assertTrue(json.currentIsSet);
    assertTrue(json.current);
  }

  @Test
  public void setOutcomeTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setOutcome("outcome");

    // Assert
    assertEquals("outcome", json.outcome);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setReasonTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setReason("because");

    // Assert
    assertEquals("because", json.reason);
  }

  @Test
  public void setStateTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setState(StatisticValueState.EXEMPT);

    // Assert
    assertEquals(StatisticValueState.EXEMPT, json.state);
  }

  @Test
  public void setStatisticIdTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setStatisticId(123L);

    // Assert
    assertTrue(json.statisticIdIsSet);
    assertEquals(123L, json.statisticId);
  }

  @Test
  public void setValueTest() {
    // Arrange
    ImmutableEntityStatisticValue.Json json = new ImmutableEntityStatisticValue.Json();

    // Act
    json.setValue("value");

    // Assert
    assertEquals("value", json.value);
  }
}

