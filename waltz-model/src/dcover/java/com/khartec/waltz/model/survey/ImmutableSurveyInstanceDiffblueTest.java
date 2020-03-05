package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.IdProvider;
import org.junit.Test;

public class ImmutableSurveyInstanceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyInstance.Json actualJson = new ImmutableSurveyInstance.Json();

    // Assert
    assertNull(actualJson.surveyEntityExternalId);
    assertNull(actualJson.submittedBy);
    assertNull(actualJson.surveyEntity);
    assertNull(actualJson.originalInstanceId);
    assertNull(actualJson.dueDate);
    assertNull(actualJson.surveyRunId);
    assertNull(actualJson.approvedBy);
    assertNull(actualJson.approvedAt);
    assertNull(actualJson.status);
    assertNull(actualJson.submittedAt);
  }
  @Test
  public void setApprovedByTest() {
    // Arrange
    ImmutableSurveyInstance.Json json = new ImmutableSurveyInstance.Json();

    // Act
    json.setApprovedBy("approvedBy");

    // Assert
    assertEquals("approvedBy", json.approvedBy);
  }
  @Test
  public void setOriginalInstanceIdTest() {
    // Arrange
    ImmutableSurveyInstance.Json json = new ImmutableSurveyInstance.Json();

    // Act
    json.setOriginalInstanceId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.originalInstanceId);
  }
  @Test
  public void setStatusTest() {
    // Arrange
    ImmutableSurveyInstance.Json json = new ImmutableSurveyInstance.Json();

    // Act
    json.setStatus(SurveyInstanceStatus.NOT_STARTED);

    // Assert
    assertEquals(SurveyInstanceStatus.NOT_STARTED, json.status);
  }
  @Test
  public void setSubmittedByTest() {
    // Arrange
    ImmutableSurveyInstance.Json json = new ImmutableSurveyInstance.Json();

    // Act
    json.setSubmittedBy("submittedBy");

    // Assert
    assertEquals("submittedBy", json.submittedBy);
  }
  @Test
  public void setSurveyEntityExternalIdTest() {
    // Arrange
    ImmutableSurveyInstance.Json json = new ImmutableSurveyInstance.Json();

    // Act
    json.setSurveyEntityExternalId("123");

    // Assert
    assertEquals("123", json.surveyEntityExternalId);
  }
  @Test
  public void setSurveyRunIdTest() {
    // Arrange
    ImmutableSurveyInstance.Json json = new ImmutableSurveyInstance.Json();

    // Act
    json.setSurveyRunId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyRunId);
  }
}

