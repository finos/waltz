package com.khartec.waltz.model.entity_workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityWorkflowStateDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityWorkflowState.Json actualJson = new ImmutableEntityWorkflowState.Json();

    // Assert
    assertNull(actualJson.entityReference);
    assertNull(actualJson.description);
    assertNull(actualJson.state);
    assertEquals(0L, actualJson.workflowId);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.workflowIdIsSet);
    assertNull(actualJson.lastUpdatedAt);
  }





  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityWorkflowState.Json json = new ImmutableEntityWorkflowState.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableEntityWorkflowState.Json json = new ImmutableEntityWorkflowState.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityWorkflowState.Json json = new ImmutableEntityWorkflowState.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setStateTest() {
    // Arrange
    ImmutableEntityWorkflowState.Json json = new ImmutableEntityWorkflowState.Json();

    // Act
    json.setState("state");

    // Assert
    assertEquals("state", json.state);
  }

  @Test
  public void setWorkflowIdTest() {
    // Arrange
    ImmutableEntityWorkflowState.Json json = new ImmutableEntityWorkflowState.Json();

    // Act
    json.setWorkflowId(123L);

    // Assert
    assertEquals(123L, json.workflowId);
    assertTrue(json.workflowIdIsSet);
  }
}

