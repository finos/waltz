package com.khartec.waltz.model.performance_metric.pack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.checkpoint.CheckpointGoal;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableMetricPackItemDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableMetricPackItem.Json()).goals.size());
  }
  @Test
  public void setBaseLineTest() {
    // Arrange
    ImmutableMetricPackItem.Json json = new ImmutableMetricPackItem.Json();

    // Act
    json.setBaseLine(10.0);

    // Assert
    assertTrue(json.baseLineIsSet);
    assertEquals(10.0, json.baseLine, 0.0);
  }
  @Test
  public void setDefinitionIdTest() {
    // Arrange
    ImmutableMetricPackItem.Json json = new ImmutableMetricPackItem.Json();

    // Act
    json.setDefinitionId(123L);

    // Assert
    assertTrue(json.definitionIdIsSet);
    assertEquals(123L, json.definitionId);
  }
  @Test
  public void setGoalsTest() {
    // Arrange
    ImmutableMetricPackItem.Json json = new ImmutableMetricPackItem.Json();
    ArrayList<CheckpointGoal> checkpointGoalList = new ArrayList<CheckpointGoal>();
    checkpointGoalList.add(null);

    // Act
    json.setGoals(checkpointGoalList);

    // Assert
    assertSame(checkpointGoalList, json.goals);
  }
  @Test
  public void setSectionNameTest() {
    // Arrange
    ImmutableMetricPackItem.Json json = new ImmutableMetricPackItem.Json();

    // Act
    json.setSectionName("sectionName");

    // Assert
    assertEquals("sectionName", json.sectionName);
  }
}

