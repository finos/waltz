package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableSurveyInstanceStatusChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableSurveyInstanceStatusChangeCommand.Json()).newStatus);
  }
  @Test
  public void setNewStatusTest() {
    // Arrange
    ImmutableSurveyInstanceStatusChangeCommand.Json json = new ImmutableSurveyInstanceStatusChangeCommand.Json();

    // Act
    json.setNewStatus(SurveyInstanceStatus.NOT_STARTED);

    // Assert
    assertEquals(SurveyInstanceStatus.NOT_STARTED, json.newStatus);
  }
}

