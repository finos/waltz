package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSurveyRunStatusChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableSurveyRunStatusChangeCommand.Json()).newStatus);
  }
  @Test
  public void setNewStatusTest() {
    // Arrange
    ImmutableSurveyRunStatusChangeCommand.Json json = new ImmutableSurveyRunStatusChangeCommand.Json();

    // Act
    json.setNewStatus(SurveyRunStatus.DRAFT);

    // Assert
    assertEquals(SurveyRunStatus.DRAFT, json.newStatus);
  }
}

