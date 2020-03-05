package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableSurveyInstanceQuestionResponseDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyInstanceQuestionResponse.Json actualJson = new ImmutableSurveyInstanceQuestionResponse.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.personId);
    assertNull(actualJson.surveyInstanceId);
    assertNull(actualJson.questionResponse);
  }
  @Test
  public void setPersonIdTest() {
    // Arrange
    ImmutableSurveyInstanceQuestionResponse.Json json = new ImmutableSurveyInstanceQuestionResponse.Json();

    // Act
    json.setPersonId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.personId);
  }
  @Test
  public void setQuestionResponseTest() {
    // Arrange
    ImmutableSurveyInstanceQuestionResponse.Json json = new ImmutableSurveyInstanceQuestionResponse.Json();
    ImmutableSurveyQuestionResponse.Json json1 = new ImmutableSurveyQuestionResponse.Json();

    // Act
    json.setQuestionResponse(json1);

    // Assert
    assertSame(json1, json.questionResponse);
  }
  @Test
  public void setSurveyInstanceIdTest() {
    // Arrange
    ImmutableSurveyInstanceQuestionResponse.Json json = new ImmutableSurveyInstanceQuestionResponse.Json();

    // Act
    json.setSurveyInstanceId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyInstanceId);
  }
}

