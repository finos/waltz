package com.khartec.waltz.model.assessment_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CommentProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableSaveAssessmentRatingCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSaveAssessmentRatingCommand.Json actualJson = new ImmutableSaveAssessmentRatingCommand.Json();

    // Assert
    assertFalse(actualJson.ratingIdIsSet);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.assessmentDefinitionIdIsSet);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.entityReference);
    assertEquals(0L, actualJson.ratingId);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.comment);
    assertEquals(0L, actualJson.assessmentDefinitionId);
  }
  @Test
  public void setAssessmentDefinitionIdTest() {
    // Arrange
    ImmutableSaveAssessmentRatingCommand.Json json = new ImmutableSaveAssessmentRatingCommand.Json();

    // Act
    json.setAssessmentDefinitionId(123L);

    // Assert
    assertTrue(json.assessmentDefinitionIdIsSet);
    assertEquals(123L, json.assessmentDefinitionId);
  }
  @Test
  public void setCommentTest() {
    // Arrange
    ImmutableSaveAssessmentRatingCommand.Json json = new ImmutableSaveAssessmentRatingCommand.Json();

    // Act
    json.setComment("comment");

    // Assert
    assertEquals("comment", json.comment);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableSaveAssessmentRatingCommand.Json json = new ImmutableSaveAssessmentRatingCommand.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableSaveAssessmentRatingCommand.Json json = new ImmutableSaveAssessmentRatingCommand.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setRatingIdTest() {
    // Arrange
    ImmutableSaveAssessmentRatingCommand.Json json = new ImmutableSaveAssessmentRatingCommand.Json();

    // Act
    json.setRatingId(123L);

    // Assert
    assertTrue(json.ratingIdIsSet);
    assertEquals(123L, json.ratingId);
  }
}

