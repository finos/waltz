package com.khartec.waltz.model.authoritativesource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableNonAuthoritativeSourceDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableNonAuthoritativeSource.Json actualJson = new ImmutableNonAuthoritativeSource.Json();

    // Assert
    assertNull(actualJson.sourceReference);
    assertEquals(0, actualJson.count);
    assertEquals(0L, actualJson.dataTypeId);
    assertFalse(actualJson.countIsSet);
    assertFalse(actualJson.dataTypeIdIsSet);
  }


  @Test
  public void setCountTest() {
    // Arrange
    ImmutableNonAuthoritativeSource.Json json = new ImmutableNonAuthoritativeSource.Json();

    // Act
    json.setCount(3);

    // Assert
    assertEquals(3, json.count);
    assertTrue(json.countIsSet);
  }

  @Test
  public void setDataTypeIdTest() {
    // Arrange
    ImmutableNonAuthoritativeSource.Json json = new ImmutableNonAuthoritativeSource.Json();

    // Act
    json.setDataTypeId(123L);

    // Assert
    assertEquals(123L, json.dataTypeId);
    assertTrue(json.dataTypeIdIsSet);
  }
}

