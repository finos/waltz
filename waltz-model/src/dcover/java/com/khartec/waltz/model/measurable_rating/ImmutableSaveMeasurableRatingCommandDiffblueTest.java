package com.khartec.waltz.model.measurable_rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableSaveMeasurableRatingCommandDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSaveMeasurableRatingCommand.Json actualJson = new ImmutableSaveMeasurableRatingCommand.Json();

    // Assert
    assertNull(actualJson.provenance);
    assertFalse(actualJson.ratingIsSet);
    assertFalse(actualJson.measurableIdIsSet);
    assertNull(actualJson.description);
    assertEquals('\u0000', actualJson.rating);
    assertNull(actualJson.lastUpdate);
    assertNull(actualJson.entityReference);
    assertEquals(0L, actualJson.measurableId);
  }





  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSaveMeasurableRatingCommand.Json json = new ImmutableSaveMeasurableRatingCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setMeasurableIdTest() {
    // Arrange
    ImmutableSaveMeasurableRatingCommand.Json json = new ImmutableSaveMeasurableRatingCommand.Json();

    // Act
    json.setMeasurableId(123L);

    // Assert
    assertTrue(json.measurableIdIsSet);
    assertEquals(123L, json.measurableId);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableSaveMeasurableRatingCommand.Json json = new ImmutableSaveMeasurableRatingCommand.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableSaveMeasurableRatingCommand.Json json = new ImmutableSaveMeasurableRatingCommand.Json();

    // Act
    json.setRating('A');

    // Assert
    assertTrue(json.ratingIsSet);
    assertEquals('A', json.rating);
  }
}

