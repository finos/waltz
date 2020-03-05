package com.khartec.waltz.model.authoritativesource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.junit.Test;

public class ImmutableAuthoritativeSourceUpdateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAuthoritativeSourceUpdateCommand.Json actualJson = new ImmutableAuthoritativeSourceUpdateCommand.Json();

    // Assert
    assertNull(actualJson.description);
    assertNull(actualJson.rating);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAuthoritativeSourceUpdateCommand.Json json = new ImmutableAuthoritativeSourceUpdateCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableAuthoritativeSourceUpdateCommand.Json json = new ImmutableAuthoritativeSourceUpdateCommand.Json();

    // Act
    json.setRating(AuthoritativenessRating.PRIMARY);

    // Assert
    assertEquals(AuthoritativenessRating.PRIMARY, json.rating);
  }
}

