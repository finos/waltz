package com.khartec.waltz.model.complexity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableComplexityScoreDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableComplexityScore.Json actualJson = new ImmutableComplexityScore.Json();

    // Assert
    assertEquals(0L, actualJson.id);
    assertFalse(actualJson.scoreIsSet);
    assertNull(actualJson.kind);
    assertEquals(0.0, actualJson.score, 0.0);
    assertFalse(actualJson.idIsSet);
  }


  @Test
  public void setIdTest() {
    // Arrange
    ImmutableComplexityScore.Json json = new ImmutableComplexityScore.Json();

    // Act
    json.setId(123L);

    // Assert
    assertEquals(123L, json.id);
    assertTrue(json.idIsSet);
  }

  @Test
  public void setKindTest() {
    // Arrange
    ImmutableComplexityScore.Json json = new ImmutableComplexityScore.Json();

    // Act
    json.setKind(ComplexityKind.CONNECTION);

    // Assert
    assertEquals(ComplexityKind.CONNECTION, json.kind);
  }

  @Test
  public void setScoreTest() {
    // Arrange
    ImmutableComplexityScore.Json json = new ImmutableComplexityScore.Json();

    // Act
    json.setScore(10.0);

    // Assert
    assertTrue(json.scoreIsSet);
    assertEquals(10.0, json.score, 0.0);
  }
}

