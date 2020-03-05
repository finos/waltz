package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutablePhysicalFlowUploadCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowUploadCommand.Json actualJson = new ImmutablePhysicalFlowUploadCommand.Json();

    // Assert
    assertNull(actualJson.target);
    assertNull(actualJson.basisOffset);
    assertNull(actualJson.name);
    assertNull(actualJson.specDescription);
    assertNull(actualJson.specExternalId);
    assertNull(actualJson.owner);
    assertNull(actualJson.dataType);
    assertNull(actualJson.transport);
    assertNull(actualJson.source);
    assertNull(actualJson.externalId);
    assertNull(actualJson.description);
    assertNull(actualJson.criticality);
    assertNull(actualJson.format);
    assertNull(actualJson.frequency);
  }
  @Test
  public void setBasisOffsetTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setBasisOffset("basisOffset");

    // Assert
    assertEquals("basisOffset", json.basisOffset);
  }
  @Test
  public void setCriticalityTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setCriticality("criticality");

    // Assert
    assertEquals("criticality", json.criticality);
  }
  @Test
  public void setDataTypeTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setDataType("dataType");

    // Assert
    assertEquals("dataType", json.dataType);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setExternalIdTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setExternalId("123");

    // Assert
    assertEquals("123", json.externalId);
  }
  @Test
  public void setFormatTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setFormat("format");

    // Assert
    assertEquals("format", json.format);
  }
  @Test
  public void setFrequencyTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setFrequency("frequency");

    // Assert
    assertEquals("frequency", json.frequency);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setOwnerTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setOwner("owner");

    // Assert
    assertEquals("owner", json.owner);
  }
  @Test
  public void setSourceTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setSource("source");

    // Assert
    assertEquals("source", json.source);
  }
  @Test
  public void setSpecDescriptionTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setSpecDescription("specDescription");

    // Assert
    assertEquals("specDescription", json.specDescription);
  }
  @Test
  public void setSpecExternalIdTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setSpecExternalId("123");

    // Assert
    assertEquals("123", json.specExternalId);
  }
  @Test
  public void setTargetTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setTarget("target");

    // Assert
    assertEquals("target", json.target);
  }
  @Test
  public void setTransportTest() {
    // Arrange
    ImmutablePhysicalFlowUploadCommand.Json json = new ImmutablePhysicalFlowUploadCommand.Json();

    // Act
    json.setTransport("transport");

    // Assert
    assertEquals("transport", json.transport);
  }
}

