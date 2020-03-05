package com.khartec.waltz.model.entity_workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityWorkflowTransitionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityWorkflowTransition.Json actualJson = new ImmutableEntityWorkflowTransition.Json();

    // Assert
    assertNull(actualJson.fromState);
    assertEquals(0L, actualJson.workflowId);
    assertNull(actualJson.lastUpdatedAt);
    assertFalse(actualJson.workflowIdIsSet);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.reason);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.toState);
  }
  @Test
  public void setFromStateTest() {
    // Arrange
    ImmutableEntityWorkflowTransition.Json json = new ImmutableEntityWorkflowTransition.Json();

    // Act
    json.setFromState("fromState");

    // Assert
    assertEquals("fromState", json.fromState);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableEntityWorkflowTransition.Json json = new ImmutableEntityWorkflowTransition.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityWorkflowTransition.Json json = new ImmutableEntityWorkflowTransition.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setReasonTest() {
    // Arrange
    ImmutableEntityWorkflowTransition.Json json = new ImmutableEntityWorkflowTransition.Json();

    // Act
    json.setReason("because");

    // Assert
    assertEquals("because", json.reason);
  }
  @Test
  public void setToStateTest() {
    // Arrange
    ImmutableEntityWorkflowTransition.Json json = new ImmutableEntityWorkflowTransition.Json();

    // Act
    json.setToState("toState");

    // Assert
    assertEquals("toState", json.toState);
  }
  @Test
  public void setWorkflowIdTest() {
    // Arrange
    ImmutableEntityWorkflowTransition.Json json = new ImmutableEntityWorkflowTransition.Json();

    // Act
    json.setWorkflowId(123L);

    // Assert
    assertEquals(123L, json.workflowId);
    assertTrue(json.workflowIdIsSet);
  }
}

