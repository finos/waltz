package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import org.junit.Test;

public class ImmutablePhysicalFlowParsedDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalFlowParsed.Json actualJson = new ImmutablePhysicalFlowParsed.Json();

    // Assert
    assertNull(actualJson.owner);
    assertNull(actualJson.specExternalId);
    assertNull(actualJson.specDescription);
    assertNull(actualJson.name);
    assertNull(actualJson.basisOffset);
    assertNull(actualJson.frequency);
    assertNull(actualJson.target);
    assertNull(actualJson.transport);
    assertNull(actualJson.source);
    assertNull(actualJson.criticality);
    assertNull(actualJson.format);
    assertNull(actualJson.externalId);
    assertNull(actualJson.description);
    assertNull(actualJson.dataType);
  }
  @Test
  public void setBasisOffsetTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setBasisOffset(1);

    // Assert
    assertEquals(Integer.valueOf(1), json.basisOffset);
  }
  @Test
  public void setCriticalityTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setCriticality(Criticality.LOW);

    // Assert
    assertEquals(Criticality.LOW, json.criticality);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setExternalIdTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setExternalId("123");

    // Assert
    assertEquals("123", json.externalId);
  }
  @Test
  public void setFormatTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setFormat(DataFormatKind.BINARY);

    // Assert
    assertEquals(DataFormatKind.BINARY, json.format);
  }
  @Test
  public void setFrequencyTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setFrequency(FrequencyKind.ON_DEMAND);

    // Assert
    assertEquals(FrequencyKind.ON_DEMAND, json.frequency);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setSpecDescriptionTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setSpecDescription("specDescription");

    // Assert
    assertEquals("specDescription", json.specDescription);
  }
  @Test
  public void setSpecExternalIdTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setSpecExternalId("123");

    // Assert
    assertEquals("123", json.specExternalId);
  }
  @Test
  public void setTransportTest() {
    // Arrange
    ImmutablePhysicalFlowParsed.Json json = new ImmutablePhysicalFlowParsed.Json();

    // Act
    json.setTransport("transport");

    // Assert
    assertEquals("transport", json.transport);
  }
}

