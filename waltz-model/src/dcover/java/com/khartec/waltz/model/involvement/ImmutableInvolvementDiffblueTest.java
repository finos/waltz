package com.khartec.waltz.model.involvement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableInvolvementDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableInvolvement.Json actualJson = new ImmutableInvolvement.Json();

    // Assert
    assertEquals(0L, actualJson.kindId);
    assertFalse(actualJson.kindIdIsSet);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.isReadOnly);
    assertNull(actualJson.entityReference);
    assertFalse(actualJson.isReadOnlyIsSet);
    assertNull(actualJson.employeeId);
  }
  @Test
  public void setEmployeeIdTest() {
    // Arrange
    ImmutableInvolvement.Json json = new ImmutableInvolvement.Json();

    // Act
    json.setEmployeeId("123");

    // Assert
    assertEquals("123", json.employeeId);
  }
  @Test
  public void setIsReadOnlyTest() {
    // Arrange
    ImmutableInvolvement.Json json = new ImmutableInvolvement.Json();

    // Act
    json.setIsReadOnly(true);

    // Assert
    assertTrue(json.isReadOnly);
    assertTrue(json.isReadOnlyIsSet);
  }
  @Test
  public void setKindIdTest() {
    // Arrange
    ImmutableInvolvement.Json json = new ImmutableInvolvement.Json();

    // Act
    json.setKindId(123L);

    // Assert
    assertEquals(123L, json.kindId);
    assertTrue(json.kindIdIsSet);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableInvolvement.Json json = new ImmutableInvolvement.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

