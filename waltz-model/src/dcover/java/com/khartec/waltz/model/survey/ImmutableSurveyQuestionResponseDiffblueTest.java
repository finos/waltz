package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.EntityReference;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSurveyQuestionResponseDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyQuestionResponse.Json actualJson = new ImmutableSurveyQuestionResponse.Json();

    // Assert
    Optional<EntityReference> optional = actualJson.entityResponse;
    assertSame(actualJson.stringResponse, optional);
    assertSame(optional, actualJson.stringResponse);
    assertSame(optional, actualJson.listResponse);
    assertSame(optional, actualJson.booleanResponse);
    assertSame(optional, actualJson.numberResponse);
    assertSame(optional, actualJson.dateResponse);
    assertSame(optional, actualJson.comment);
  }
  @Test
  public void setQuestionIdTest() {
    // Arrange
    ImmutableSurveyQuestionResponse.Json json = new ImmutableSurveyQuestionResponse.Json();

    // Act
    json.setQuestionId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.questionId);
  }
}

