package com.khartec.waltz.model.complexity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import org.junit.Test;

public class ImmutableComplexityRatingDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableComplexityRating.Json actualJson = new ImmutableComplexityRating.Json();

    // Assert
    Optional<ComplexityScore> optional = actualJson.serverComplexity;
    assertSame(actualJson.measurableComplexity, optional);
    assertSame(optional, actualJson.connectionComplexity);
    assertSame(optional, actualJson.measurableComplexity);
  }


  @Test
  public void setIdTest() {
    // Arrange
    ImmutableComplexityRating.Json json = new ImmutableComplexityRating.Json();

    // Act
    json.setId(123L);

    // Assert
    assertTrue(json.idIsSet);
    assertEquals(123L, json.id);
  }
}

