package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSurveyInstanceRecipientCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyInstanceRecipientCreateCommand.Json actualJson = new ImmutableSurveyInstanceRecipientCreateCommand.Json();

    // Assert
    assertNull(actualJson.personId);
    assertNull(actualJson.surveyInstanceId);
  }
  @Test
  public void setPersonIdTest() {
    // Arrange
    ImmutableSurveyInstanceRecipientCreateCommand.Json json = new ImmutableSurveyInstanceRecipientCreateCommand.Json();

    // Act
    json.setPersonId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.personId);
  }
  @Test
  public void setSurveyInstanceIdTest() {
    // Arrange
    ImmutableSurveyInstanceRecipientCreateCommand.Json json = new ImmutableSurveyInstanceRecipientCreateCommand.Json();

    // Act
    json.setSurveyInstanceId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyInstanceId);
  }
}

