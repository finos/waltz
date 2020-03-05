package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableSurveyRunCompletionRateDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyRunCompletionRate.Json actualJson = new ImmutableSurveyRunCompletionRate.Json();

    // Assert
    assertFalse(actualJson.inProgressCountIsSet);
    assertFalse(actualJson.surveyRunIdIsSet);
    assertEquals(0L, actualJson.surveyRunId);
    assertEquals(0, actualJson.inProgressCount);
    assertFalse(actualJson.notStartedCountIsSet);
    assertEquals(0, actualJson.completedCount);
    assertEquals(0, actualJson.notStartedCount);
    assertFalse(actualJson.completedCountIsSet);
  }


  @Test
  public void setCompletedCountTest() {
    // Arrange
    ImmutableSurveyRunCompletionRate.Json json = new ImmutableSurveyRunCompletionRate.Json();

    // Act
    json.setCompletedCount(3);

    // Assert
    assertEquals(3, json.completedCount);
    assertTrue(json.completedCountIsSet);
  }

  @Test
  public void setInProgressCountTest() {
    // Arrange
    ImmutableSurveyRunCompletionRate.Json json = new ImmutableSurveyRunCompletionRate.Json();

    // Act
    json.setInProgressCount(3);

    // Assert
    assertTrue(json.inProgressCountIsSet);
    assertEquals(3, json.inProgressCount);
  }

  @Test
  public void setNotStartedCountTest() {
    // Arrange
    ImmutableSurveyRunCompletionRate.Json json = new ImmutableSurveyRunCompletionRate.Json();

    // Act
    json.setNotStartedCount(3);

    // Assert
    assertTrue(json.notStartedCountIsSet);
    assertEquals(3, json.notStartedCount);
  }

  @Test
  public void setSurveyRunIdTest() {
    // Arrange
    ImmutableSurveyRunCompletionRate.Json json = new ImmutableSurveyRunCompletionRate.Json();

    // Act
    json.setSurveyRunId(123L);

    // Assert
    assertTrue(json.surveyRunIdIsSet);
    assertEquals(123L, json.surveyRunId);
  }
}

