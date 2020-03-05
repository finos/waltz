package com.khartec.waltz.model.data_flow_decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.FlowDirection;
import org.junit.Test;

public class ImmutableDataTypeDirectionKeyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDataTypeDirectionKey.Json actualJson = new ImmutableDataTypeDirectionKey.Json();

    // Assert
    assertNull(actualJson.flowDirection);
    assertNull(actualJson.DatatypeId);
  }
  @Test
  public void setDatatypeIdTest() {
    // Arrange
    ImmutableDataTypeDirectionKey.Json json = new ImmutableDataTypeDirectionKey.Json();

    // Act
    json.setDatatypeId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.DatatypeId);
  }
  @Test
  public void setFlowDirectionTest() {
    // Arrange
    ImmutableDataTypeDirectionKey.Json json = new ImmutableDataTypeDirectionKey.Json();

    // Act
    json.setFlowDirection(FlowDirection.INBOUND);

    // Assert
    assertEquals(FlowDirection.INBOUND, json.flowDirection);
  }
}

