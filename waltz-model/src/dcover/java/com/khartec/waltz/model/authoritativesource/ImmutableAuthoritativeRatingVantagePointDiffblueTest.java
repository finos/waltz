package com.khartec.waltz.model.authoritativesource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.junit.Test;

public class ImmutableAuthoritativeRatingVantagePointDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAuthoritativeRatingVantagePoint.Json actualJson = new ImmutableAuthoritativeRatingVantagePoint.Json();

    // Assert
    assertNull(actualJson.applicationId);
    assertNull(actualJson.vantagePoint);
    assertFalse(actualJson.dataTypeRankIsSet);
    assertEquals(0, actualJson.vantagePointRank);
    assertNull(actualJson.dataTypeCode);
    assertEquals(0, actualJson.dataTypeRank);
    assertNull(actualJson.rating);
    assertFalse(actualJson.vantagePointRankIsSet);
  }
  @Test
  public void setApplicationIdTest() {
    // Arrange
    ImmutableAuthoritativeRatingVantagePoint.Json json = new ImmutableAuthoritativeRatingVantagePoint.Json();

    // Act
    json.setApplicationId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.applicationId);
  }
  @Test
  public void setDataTypeCodeTest() {
    // Arrange
    ImmutableAuthoritativeRatingVantagePoint.Json json = new ImmutableAuthoritativeRatingVantagePoint.Json();

    // Act
    json.setDataTypeCode("dataTypeCode");

    // Assert
    assertEquals("dataTypeCode", json.dataTypeCode);
  }
  @Test
  public void setDataTypeRankTest() {
    // Arrange
    ImmutableAuthoritativeRatingVantagePoint.Json json = new ImmutableAuthoritativeRatingVantagePoint.Json();

    // Act
    json.setDataTypeRank(1);

    // Assert
    assertTrue(json.dataTypeRankIsSet);
    assertEquals(1, json.dataTypeRank);
  }
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableAuthoritativeRatingVantagePoint.Json json = new ImmutableAuthoritativeRatingVantagePoint.Json();

    // Act
    json.setRating(AuthoritativenessRating.PRIMARY);

    // Assert
    assertEquals(AuthoritativenessRating.PRIMARY, json.rating);
  }
  @Test
  public void setVantagePointRankTest() {
    // Arrange
    ImmutableAuthoritativeRatingVantagePoint.Json json = new ImmutableAuthoritativeRatingVantagePoint.Json();

    // Act
    json.setVantagePointRank(1);

    // Assert
    assertEquals(1, json.vantagePointRank);
    assertTrue(json.vantagePointRankIsSet);
  }
}

