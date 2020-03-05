package com.khartec.waltz.model.physical_specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutablePhysicalSpecificationAddCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecificationAddCommand.Json actualJson = new ImmutablePhysicalSpecificationAddCommand.Json();

    // Assert
    assertNull(actualJson.owningEntity);
    assertNull(actualJson.format);
    assertNull(actualJson.name);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalSpecificationAddCommand.Json json = new ImmutablePhysicalSpecificationAddCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setFormatTest() {
    // Arrange
    ImmutablePhysicalSpecificationAddCommand.Json json = new ImmutablePhysicalSpecificationAddCommand.Json();

    // Act
    json.setFormat(DataFormatKind.BINARY);

    // Assert
    assertEquals(DataFormatKind.BINARY, json.format);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalSpecificationAddCommand.Json json = new ImmutablePhysicalSpecificationAddCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

