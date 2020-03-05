package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableAttestationCreateSummaryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttestationCreateSummary.Json actualJson = new ImmutableAttestationCreateSummary.Json();

    // Assert
    assertFalse(actualJson.instanceCountIsSet);
    assertEquals(0L, actualJson.recipientCount);
    assertEquals(0, actualJson.entityCount);
    assertEquals(0, actualJson.instanceCount);
    assertFalse(actualJson.recipientCountIsSet);
    assertFalse(actualJson.entityCountIsSet);
  }
  @Test
  public void setEntityCountTest() {
    // Arrange
    ImmutableAttestationCreateSummary.Json json = new ImmutableAttestationCreateSummary.Json();

    // Act
    json.setEntityCount(3);

    // Assert
    assertEquals(3, json.entityCount);
    assertTrue(json.entityCountIsSet);
  }
  @Test
  public void setInstanceCountTest() {
    // Arrange
    ImmutableAttestationCreateSummary.Json json = new ImmutableAttestationCreateSummary.Json();

    // Act
    json.setInstanceCount(3);

    // Assert
    assertTrue(json.instanceCountIsSet);
    assertEquals(3, json.instanceCount);
  }
  @Test
  public void setRecipientCountTest() {
    // Arrange
    ImmutableAttestationCreateSummary.Json json = new ImmutableAttestationCreateSummary.Json();

    // Act
    json.setRecipientCount(3L);

    // Assert
    assertEquals(3L, json.recipientCount);
    assertTrue(json.recipientCountIsSet);
  }
}

