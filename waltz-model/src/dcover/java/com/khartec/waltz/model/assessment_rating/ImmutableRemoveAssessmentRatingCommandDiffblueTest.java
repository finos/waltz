package com.khartec.waltz.model.assessment_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableRemoveAssessmentRatingCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableRemoveAssessmentRatingCommand.Json actualJson = new ImmutableRemoveAssessmentRatingCommand.Json();

    // Assert
    assertEquals(0L, actualJson.assessmentDefinitionId);
    assertFalse(actualJson.assessmentDefinitionIdIsSet);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.lastUpdatedAt);
  }
  @Test
  public void setAssessmentDefinitionIdTest() {
    // Arrange
    ImmutableRemoveAssessmentRatingCommand.Json json = new ImmutableRemoveAssessmentRatingCommand.Json();

    // Act
    json.setAssessmentDefinitionId(123L);

    // Assert
    assertEquals(123L, json.assessmentDefinitionId);
    assertTrue(json.assessmentDefinitionIdIsSet);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableRemoveAssessmentRatingCommand.Json json = new ImmutableRemoveAssessmentRatingCommand.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
}

