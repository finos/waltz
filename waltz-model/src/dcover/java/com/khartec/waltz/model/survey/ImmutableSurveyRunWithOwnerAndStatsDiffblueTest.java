package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableSurveyRunWithOwnerAndStatsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyRunWithOwnerAndStats.Json actualJson = new ImmutableSurveyRunWithOwnerAndStats.Json();

    // Assert
    assertNull(actualJson.completionRateStats);
    assertNull(actualJson.owner);
    assertNull(actualJson.surveyRun);
  }
  @Test
  public void setCompletionRateStatsTest() {
    // Arrange
    ImmutableSurveyRunWithOwnerAndStats.Json json = new ImmutableSurveyRunWithOwnerAndStats.Json();
    ImmutableSurveyRunCompletionRate.Json json1 = new ImmutableSurveyRunCompletionRate.Json();

    // Act
    json.setCompletionRateStats(json1);

    // Assert
    assertSame(json1, json.completionRateStats);
  }
  @Test
  public void setSurveyRunTest() {
    // Arrange
    ImmutableSurveyRunWithOwnerAndStats.Json json = new ImmutableSurveyRunWithOwnerAndStats.Json();
    ImmutableSurveyRun.Json json1 = new ImmutableSurveyRun.Json();

    // Act
    json.setSurveyRun(json1);

    // Assert
    assertSame(json1, json.surveyRun);
  }
}

