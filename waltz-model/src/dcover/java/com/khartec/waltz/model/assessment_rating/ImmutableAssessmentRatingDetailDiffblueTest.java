package com.khartec.waltz.model.assessment_rating;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableAssessmentRatingDetailDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAssessmentRatingDetail.Json actualJson = new ImmutableAssessmentRatingDetail.Json();

    // Assert
    assertNull(actualJson.assessmentRating);
    assertNull(actualJson.ratingDefinition);
  }
  @Test
  public void setAssessmentRatingTest() {
    // Arrange
    ImmutableAssessmentRatingDetail.Json json = new ImmutableAssessmentRatingDetail.Json();
    ImmutableAssessmentRating.Json json1 = new ImmutableAssessmentRating.Json();

    // Act
    json.setAssessmentRating(json1);

    // Assert
    assertSame(json1, json.assessmentRating);
  }
}

