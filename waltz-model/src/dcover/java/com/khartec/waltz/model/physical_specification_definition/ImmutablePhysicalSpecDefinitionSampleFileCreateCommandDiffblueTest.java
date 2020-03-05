package com.khartec.waltz.model.physical_specification_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutablePhysicalSpecDefinitionSampleFileCreateCommandDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.Json actualJson = new ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.Json();

    // Assert
    assertNull(actualJson.fileData);
    assertNull(actualJson.name);
  }

  @Test
  public void setFileDataTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.Json json = new ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.Json();

    // Act
    json.setFileData("fileData");

    // Assert
    assertEquals("fileData", json.fileData);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.Json json = new ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

