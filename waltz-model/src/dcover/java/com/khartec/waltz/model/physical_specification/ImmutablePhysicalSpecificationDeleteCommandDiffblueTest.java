package com.khartec.waltz.model.physical_specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutablePhysicalSpecificationDeleteCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecificationDeleteCommand.Json actualJson = new ImmutablePhysicalSpecificationDeleteCommand.Json();

    // Assert
    assertFalse(actualJson.specificationIdIsSet);
    assertEquals(0L, actualJson.specificationId);
  }
  @Test
  public void setSpecificationIdTest() {
    // Arrange
    ImmutablePhysicalSpecificationDeleteCommand.Json json = new ImmutablePhysicalSpecificationDeleteCommand.Json();

    // Act
    json.setSpecificationId(123L);

    // Assert
    assertTrue(json.specificationIdIsSet);
    assertEquals(123L, json.specificationId);
  }
}

