package com.khartec.waltz.model.physical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.DescriptionProvider;
import org.junit.Test;

public class ImmutableFlowAttributesDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableFlowAttributes.Json actualJson = new ImmutableFlowAttributes.Json();

    // Assert
    assertEquals(0, actualJson.basisOffset);
    assertNull(actualJson.criticality);
    assertNull(actualJson.description);
    assertNull(actualJson.transport);
    assertFalse(actualJson.basisOffsetIsSet);
    assertNull(actualJson.frequency);
  }



  @Test
  public void setBasisOffsetTest() {
    // Arrange
    ImmutableFlowAttributes.Json json = new ImmutableFlowAttributes.Json();

    // Act
    json.setBasisOffset(1);

    // Assert
    assertEquals(1, json.basisOffset);
    assertTrue(json.basisOffsetIsSet);
  }

  @Test
  public void setCriticalityTest() {
    // Arrange
    ImmutableFlowAttributes.Json json = new ImmutableFlowAttributes.Json();

    // Act
    json.setCriticality(Criticality.LOW);

    // Assert
    assertEquals(Criticality.LOW, json.criticality);
  }

  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableFlowAttributes.Json json = new ImmutableFlowAttributes.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setFrequencyTest() {
    // Arrange
    ImmutableFlowAttributes.Json json = new ImmutableFlowAttributes.Json();

    // Act
    json.setFrequency(FrequencyKind.ON_DEMAND);

    // Assert
    assertEquals(FrequencyKind.ON_DEMAND, json.frequency);
  }

  @Test
  public void setTransportTest() {
    // Arrange
    ImmutableFlowAttributes.Json json = new ImmutableFlowAttributes.Json();

    // Act
    json.setTransport("transport");

    // Assert
    assertEquals("transport", json.transport);
  }
}

