package com.khartec.waltz.model.source_data_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.rating.RagRating;
import org.junit.Test;

public class ImmutableSourceDataRatingDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSourceDataRating.Json actualJson = new ImmutableSourceDataRating.Json();

    // Assert
    assertNull(actualJson.entityKind);
    assertNull(actualJson.completeness);
    assertNull(actualJson.sourceName);
    assertNull(actualJson.accuracy);
    assertNull(actualJson.authoritativeness);
  }


  @Test
  public void setAccuracyTest() {
    // Arrange
    ImmutableSourceDataRating.Json json = new ImmutableSourceDataRating.Json();

    // Act
    json.setAccuracy(RagRating.R);

    // Assert
    assertEquals(RagRating.R, json.accuracy);
  }

  @Test
  public void setAuthoritativenessTest() {
    // Arrange
    ImmutableSourceDataRating.Json json = new ImmutableSourceDataRating.Json();

    // Act
    json.setAuthoritativeness(RagRating.R);

    // Assert
    assertEquals(RagRating.R, json.authoritativeness);
  }

  @Test
  public void setCompletenessTest() {
    // Arrange
    ImmutableSourceDataRating.Json json = new ImmutableSourceDataRating.Json();

    // Act
    json.setCompleteness(RagRating.R);

    // Assert
    assertEquals(RagRating.R, json.completeness);
  }

  @Test
  public void setEntityKindTest() {
    // Arrange
    ImmutableSourceDataRating.Json json = new ImmutableSourceDataRating.Json();

    // Act
    json.setEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.entityKind);
  }

  @Test
  public void setSourceNameTest() {
    // Arrange
    ImmutableSourceDataRating.Json json = new ImmutableSourceDataRating.Json();

    // Act
    json.setSourceName("sourceName");

    // Assert
    assertEquals("sourceName", json.sourceName);
  }
}

