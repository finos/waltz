package com.khartec.waltz.model.allocation_scheme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableAllocationSchemeDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAllocationScheme.Json actualJson = new ImmutableAllocationScheme.Json();

    // Assert
    assertFalse(actualJson.measurableCategoryIdIsSet);
    assertNull(actualJson.description);
    assertEquals(0L, actualJson.measurableCategoryId);
    assertNull(actualJson.name);
  }





  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAllocationScheme.Json json = new ImmutableAllocationScheme.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setMeasurableCategoryIdTest() {
    // Arrange
    ImmutableAllocationScheme.Json json = new ImmutableAllocationScheme.Json();

    // Act
    json.setMeasurableCategoryId(123L);

    // Assert
    assertTrue(json.measurableCategoryIdIsSet);
    assertEquals(123L, json.measurableCategoryId);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAllocationScheme.Json json = new ImmutableAllocationScheme.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

