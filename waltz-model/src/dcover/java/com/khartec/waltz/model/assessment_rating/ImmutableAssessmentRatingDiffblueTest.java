package com.khartec.waltz.model.assessment_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CommentProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableAssessmentRatingDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAssessmentRating.Json actualJson = new ImmutableAssessmentRating.Json();

    // Assert
    assertEquals(0L, actualJson.ratingId);
    assertEquals(0L, actualJson.assessmentDefinitionId);
    assertNull(actualJson.comment);
    assertFalse(actualJson.ratingIdIsSet);
    assertFalse(actualJson.assessmentDefinitionIdIsSet);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.entityReference);
  }
  @Test
  public void setAssessmentDefinitionIdTest() {
    // Arrange
    ImmutableAssessmentRating.Json json = new ImmutableAssessmentRating.Json();

    // Act
    json.setAssessmentDefinitionId(123L);

    // Assert
    assertEquals(123L, json.assessmentDefinitionId);
    assertTrue(json.assessmentDefinitionIdIsSet);
  }
  @Test
  public void setCommentTest() {
    // Arrange
    ImmutableAssessmentRating.Json json = new ImmutableAssessmentRating.Json();

    // Act
    json.setComment("comment");

    // Assert
    assertEquals("comment", json.comment);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableAssessmentRating.Json json = new ImmutableAssessmentRating.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableAssessmentRating.Json json = new ImmutableAssessmentRating.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setRatingIdTest() {
    // Arrange
    ImmutableAssessmentRating.Json json = new ImmutableAssessmentRating.Json();

    // Act
    json.setRatingId(123L);

    // Assert
    assertEquals(123L, json.ratingId);
    assertTrue(json.ratingIdIsSet);
  }
}

