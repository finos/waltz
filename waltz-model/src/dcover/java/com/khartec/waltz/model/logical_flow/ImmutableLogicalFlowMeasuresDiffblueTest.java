package com.khartec.waltz.model.logical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableLogicalFlowMeasuresDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLogicalFlowMeasures.Json actualJson = new ImmutableLogicalFlowMeasures.Json();

    // Assert
    assertFalse(actualJson.inboundIsSet);
    assertEquals(0.0, actualJson.intra, 0.0);
    assertEquals(0.0, actualJson.inbound, 0.0);
    assertEquals(0.0, actualJson.outbound, 0.0);
    assertFalse(actualJson.outboundIsSet);
    assertFalse(actualJson.intraIsSet);
  }


  @Test
  public void setInboundTest() {
    // Arrange
    ImmutableLogicalFlowMeasures.Json json = new ImmutableLogicalFlowMeasures.Json();

    // Act
    json.setInbound(10.0);

    // Assert
    assertTrue(json.inboundIsSet);
    assertEquals(10.0, json.inbound, 0.0);
  }

  @Test
  public void setIntraTest() {
    // Arrange
    ImmutableLogicalFlowMeasures.Json json = new ImmutableLogicalFlowMeasures.Json();

    // Act
    json.setIntra(10.0);

    // Assert
    assertEquals(10.0, json.intra, 0.0);
    assertTrue(json.intraIsSet);
  }

  @Test
  public void setOutboundTest() {
    // Arrange
    ImmutableLogicalFlowMeasures.Json json = new ImmutableLogicalFlowMeasures.Json();

    // Act
    json.setOutbound(10.0);

    // Assert
    assertEquals(10.0, json.outbound, 0.0);
    assertTrue(json.outboundIsSet);
  }
}

