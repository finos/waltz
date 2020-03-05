package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSurveyInstanceRecipientUpdateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyInstanceRecipientUpdateCommand.Json actualJson = new ImmutableSurveyInstanceRecipientUpdateCommand.Json();

    // Assert
    assertNull(actualJson.instanceRecipientId);
    assertNull(actualJson.surveyInstanceId);
    assertNull(actualJson.personId);
  }
  @Test
  public void setInstanceRecipientIdTest() {
    // Arrange
    ImmutableSurveyInstanceRecipientUpdateCommand.Json json = new ImmutableSurveyInstanceRecipientUpdateCommand.Json();

    // Act
    json.setInstanceRecipientId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.instanceRecipientId);
  }
  @Test
  public void setPersonIdTest() {
    // Arrange
    ImmutableSurveyInstanceRecipientUpdateCommand.Json json = new ImmutableSurveyInstanceRecipientUpdateCommand.Json();

    // Act
    json.setPersonId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.personId);
  }
  @Test
  public void setSurveyInstanceIdTest() {
    // Arrange
    ImmutableSurveyInstanceRecipientUpdateCommand.Json json = new ImmutableSurveyInstanceRecipientUpdateCommand.Json();

    // Act
    json.setSurveyInstanceId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyInstanceId);
  }
}

