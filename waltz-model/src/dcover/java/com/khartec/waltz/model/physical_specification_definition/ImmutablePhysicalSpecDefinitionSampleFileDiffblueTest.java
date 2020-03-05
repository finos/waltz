package com.khartec.waltz.model.physical_specification_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutablePhysicalSpecDefinitionSampleFileDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecDefinitionSampleFile.Json actualJson = new ImmutablePhysicalSpecDefinitionSampleFile.Json();

    // Assert
    assertEquals(0L, actualJson.specDefinitionId);
    assertNull(actualJson.name);
    assertNull(actualJson.fileData);
    assertFalse(actualJson.specDefinitionIdIsSet);
  }
  @Test
  public void setFileDataTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionSampleFile.Json json = new ImmutablePhysicalSpecDefinitionSampleFile.Json();

    // Act
    json.setFileData("fileData");

    // Assert
    assertEquals("fileData", json.fileData);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionSampleFile.Json json = new ImmutablePhysicalSpecDefinitionSampleFile.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setSpecDefinitionIdTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionSampleFile.Json json = new ImmutablePhysicalSpecDefinitionSampleFile.Json();

    // Act
    json.setSpecDefinitionId(123L);

    // Assert
    assertEquals(123L, json.specDefinitionId);
    assertTrue(json.specDefinitionIdIsSet);
  }
}

