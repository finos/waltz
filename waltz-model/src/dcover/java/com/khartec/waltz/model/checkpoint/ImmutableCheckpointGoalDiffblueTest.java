package com.khartec.waltz.model.checkpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableCheckpointGoalDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableCheckpointGoal.Json actualJson = new ImmutableCheckpointGoal.Json();

    // Assert
    assertEquals(0.0, actualJson.value, 0.0);
    assertNull(actualJson.goalType);
    assertFalse(actualJson.valueIsSet);
    assertFalse(actualJson.checkpointIdIsSet);
    assertEquals(0L, actualJson.checkpointId);
  }


  @Test
  public void setCheckpointIdTest() {
    // Arrange
    ImmutableCheckpointGoal.Json json = new ImmutableCheckpointGoal.Json();

    // Act
    json.setCheckpointId(123L);

    // Assert
    assertTrue(json.checkpointIdIsSet);
    assertEquals(123L, json.checkpointId);
  }

  @Test
  public void setGoalTypeTest() {
    // Arrange
    ImmutableCheckpointGoal.Json json = new ImmutableCheckpointGoal.Json();

    // Act
    json.setGoalType(GoalType.ABOVE_THRESHOLD);

    // Assert
    assertEquals(GoalType.ABOVE_THRESHOLD, json.goalType);
  }

  @Test
  public void setValueTest() {
    // Arrange
    ImmutableCheckpointGoal.Json json = new ImmutableCheckpointGoal.Json();

    // Act
    json.setValue(10.0);

    // Assert
    assertEquals(10.0, json.value, 0.0);
    assertTrue(json.valueIsSet);
  }
}

