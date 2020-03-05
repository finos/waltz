package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableAttestationRunResponseSummaryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttestationRunResponseSummary.Json actualJson = new ImmutableAttestationRunResponseSummary.Json();

    // Assert
    assertFalse(actualJson.pendingCountIsSet);
    assertEquals(0L, actualJson.runId);
    assertFalse(actualJson.completeCountIsSet);
    assertEquals(0L, actualJson.completeCount);
    assertFalse(actualJson.runIdIsSet);
    assertEquals(0L, actualJson.pendingCount);
  }
  @Test
  public void setCompleteCountTest() {
    // Arrange
    ImmutableAttestationRunResponseSummary.Json json = new ImmutableAttestationRunResponseSummary.Json();

    // Act
    json.setCompleteCount(3L);

    // Assert
    assertTrue(json.completeCountIsSet);
    assertEquals(3L, json.completeCount);
  }
  @Test
  public void setPendingCountTest() {
    // Arrange
    ImmutableAttestationRunResponseSummary.Json json = new ImmutableAttestationRunResponseSummary.Json();

    // Act
    json.setPendingCount(3L);

    // Assert
    assertTrue(json.pendingCountIsSet);
    assertEquals(3L, json.pendingCount);
  }
  @Test
  public void setRunIdTest() {
    // Arrange
    ImmutableAttestationRunResponseSummary.Json json = new ImmutableAttestationRunResponseSummary.Json();

    // Act
    json.setRunId(123L);

    // Assert
    assertEquals(123L, json.runId);
    assertTrue(json.runIdIsSet);
  }
}

