package com.khartec.waltz.model.authoritativesource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.junit.Test;

public class ImmutableAuthoritativeSourceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAuthoritativeSource.Json actualJson = new ImmutableAuthoritativeSource.Json();

    // Assert
    assertNull(actualJson.appOrgUnitReference);
    assertNull(actualJson.applicationReference);
    assertNull(actualJson.dataType);
    assertNull(actualJson.provenance);
    assertNull(actualJson.description);
    assertNull(actualJson.rating);
    assertNull(actualJson.parentReference);
  }
  @Test
  public void setDataTypeTest() {
    // Arrange
    ImmutableAuthoritativeSource.Json json = new ImmutableAuthoritativeSource.Json();

    // Act
    json.setDataType("dataType");

    // Assert
    assertEquals("dataType", json.dataType);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAuthoritativeSource.Json json = new ImmutableAuthoritativeSource.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableAuthoritativeSource.Json json = new ImmutableAuthoritativeSource.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableAuthoritativeSource.Json json = new ImmutableAuthoritativeSource.Json();

    // Act
    json.setRating(AuthoritativenessRating.PRIMARY);

    // Assert
    assertEquals(AuthoritativenessRating.PRIMARY, json.rating);
  }
}

