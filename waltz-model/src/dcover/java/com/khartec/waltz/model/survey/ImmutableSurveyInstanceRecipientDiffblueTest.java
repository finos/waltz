package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableSurveyInstanceRecipientDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyInstanceRecipient.Json actualJson = new ImmutableSurveyInstanceRecipient.Json();

    // Assert
    assertNull(actualJson.person);
    assertNull(actualJson.surveyInstance);
  }



  @Test
  public void setSurveyInstanceTest() {
    // Arrange
    ImmutableSurveyInstanceRecipient.Json json = new ImmutableSurveyInstanceRecipient.Json();
    ImmutableSurveyInstance.Json json1 = new ImmutableSurveyInstance.Json();

    // Act
    json.setSurveyInstance(json1);

    // Assert
    assertSame(json1, json.surveyInstance);
  }
}

