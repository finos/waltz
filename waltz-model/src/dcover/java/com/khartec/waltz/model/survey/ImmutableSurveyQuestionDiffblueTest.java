package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.ExternalIdProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSurveyQuestionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyQuestion.Json actualJson = new ImmutableSurveyQuestion.Json();

    // Assert
    Optional<String> optional = actualJson.helpText;
    assertSame(optional, actualJson.sectionName);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setAllowCommentTest() {
    // Arrange
    ImmutableSurveyQuestion.Json json = new ImmutableSurveyQuestion.Json();

    // Act
    json.setAllowComment(true);

    // Assert
    assertEquals(Boolean.valueOf(true), json.allowComment);
  }
  @Test
  public void setFieldTypeTest() {
    // Arrange
    ImmutableSurveyQuestion.Json json = new ImmutableSurveyQuestion.Json();

    // Act
    json.setFieldType(SurveyQuestionFieldType.APPLICATION);

    // Assert
    assertEquals(SurveyQuestionFieldType.APPLICATION, json.fieldType);
  }
  @Test
  public void setIsMandatoryTest() {
    // Arrange
    ImmutableSurveyQuestion.Json json = new ImmutableSurveyQuestion.Json();

    // Act
    json.setIsMandatory(true);

    // Assert
    assertTrue(json.isMandatory);
    assertTrue(json.isMandatoryIsSet);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableSurveyQuestion.Json json = new ImmutableSurveyQuestion.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertEquals(Integer.valueOf(1), json.position);
  }
  @Test
  public void setQuestionTextTest() {
    // Arrange
    ImmutableSurveyQuestion.Json json = new ImmutableSurveyQuestion.Json();

    // Act
    json.setQuestionText("questionText");

    // Assert
    assertEquals("questionText", json.questionText);
  }
  @Test
  public void setSurveyTemplateIdTest() {
    // Arrange
    ImmutableSurveyQuestion.Json json = new ImmutableSurveyQuestion.Json();

    // Act
    json.setSurveyTemplateId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyTemplateId);
  }
}

