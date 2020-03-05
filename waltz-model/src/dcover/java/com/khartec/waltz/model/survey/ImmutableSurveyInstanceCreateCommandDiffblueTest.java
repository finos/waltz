package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSurveyInstanceCreateCommandDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyInstanceCreateCommand.Json actualJson = new ImmutableSurveyInstanceCreateCommand.Json();

    // Assert
    assertNull(actualJson.surveyRunId);
    assertNull(actualJson.status);
    assertNull(actualJson.entityReference);
  }


  @Test
  public void setStatusTest() {
    // Arrange
    ImmutableSurveyInstanceCreateCommand.Json json = new ImmutableSurveyInstanceCreateCommand.Json();

    // Act
    json.setStatus(SurveyInstanceStatus.NOT_STARTED);

    // Assert
    assertEquals(SurveyInstanceStatus.NOT_STARTED, json.status);
  }

  @Test
  public void setSurveyRunIdTest() {
    // Arrange
    ImmutableSurveyInstanceCreateCommand.Json json = new ImmutableSurveyInstanceCreateCommand.Json();

    // Act
    json.setSurveyRunId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyRunId);
  }
}

