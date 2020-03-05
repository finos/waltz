package com.khartec.waltz.model.data_flow_decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.junit.Test;

public class ImmutableLogicalFlowDecoratorDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLogicalFlowDecorator.Json actualJson = new ImmutableLogicalFlowDecorator.Json();

    // Assert
    assertEquals(0L, actualJson.dataFlowId);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.rating);
    assertFalse(actualJson.dataFlowIdIsSet);
    assertNull(actualJson.decoratorEntity);
  }




  @Test
  public void setDataFlowIdTest() {
    // Arrange
    ImmutableLogicalFlowDecorator.Json json = new ImmutableLogicalFlowDecorator.Json();

    // Act
    json.setDataFlowId(123L);

    // Assert
    assertEquals(123L, json.dataFlowId);
    assertTrue(json.dataFlowIdIsSet);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableLogicalFlowDecorator.Json json = new ImmutableLogicalFlowDecorator.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableLogicalFlowDecorator.Json json = new ImmutableLogicalFlowDecorator.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableLogicalFlowDecorator.Json json = new ImmutableLogicalFlowDecorator.Json();

    // Act
    json.setRating(AuthoritativenessRating.PRIMARY);

    // Assert
    assertEquals(AuthoritativenessRating.PRIMARY, json.rating);
  }
}

