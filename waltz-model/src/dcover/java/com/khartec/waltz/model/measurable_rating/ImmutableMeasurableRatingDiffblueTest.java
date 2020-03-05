package com.khartec.waltz.model.measurable_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableMeasurableRatingDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurableRating.Json actualJson = new ImmutableMeasurableRating.Json();

    // Assert
    assertEquals('\u0000', actualJson.rating);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.entityReference);
    assertEquals(0L, actualJson.measurableId);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.ratingIsSet);
    assertFalse(actualJson.measurableIdIsSet);
    assertNull(actualJson.description);
  }





  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableMeasurableRating.Json json = new ImmutableMeasurableRating.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableMeasurableRating.Json json = new ImmutableMeasurableRating.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setMeasurableIdTest() {
    // Arrange
    ImmutableMeasurableRating.Json json = new ImmutableMeasurableRating.Json();

    // Act
    json.setMeasurableId(123L);

    // Assert
    assertEquals(123L, json.measurableId);
    assertTrue(json.measurableIdIsSet);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableMeasurableRating.Json json = new ImmutableMeasurableRating.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableMeasurableRating.Json json = new ImmutableMeasurableRating.Json();

    // Act
    json.setRating('A');

    // Assert
    assertEquals('A', json.rating);
    assertTrue(json.ratingIsSet);
  }
}

