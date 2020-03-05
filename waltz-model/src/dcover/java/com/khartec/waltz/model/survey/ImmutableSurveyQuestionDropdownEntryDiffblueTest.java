package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableSurveyQuestionDropdownEntryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyQuestionDropdownEntry.Json actualJson = new ImmutableSurveyQuestionDropdownEntry.Json();

    // Assert
    assertSame(actualJson.questionId, actualJson.id);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableSurveyQuestionDropdownEntry.Json json = new ImmutableSurveyQuestionDropdownEntry.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertTrue(json.positionIsSet);
    assertEquals(1, json.position);
  }
  @Test
  public void setValueTest() {
    // Arrange
    ImmutableSurveyQuestionDropdownEntry.Json json = new ImmutableSurveyQuestionDropdownEntry.Json();

    // Act
    json.setValue("value");

    // Assert
    assertEquals("value", json.value);
  }
}

