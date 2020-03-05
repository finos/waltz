package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableAttestationInstanceRecipientDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttestationInstanceRecipient.Json actualJson = new ImmutableAttestationInstanceRecipient.Json();

    // Assert
    assertNull(actualJson.attestationInstance);
    assertNull(actualJson.userId);
  }
  @Test
  public void setAttestationInstanceTest() {
    // Arrange
    ImmutableAttestationInstanceRecipient.Json json = new ImmutableAttestationInstanceRecipient.Json();
    ImmutableAttestationInstance.Json json1 = new ImmutableAttestationInstance.Json();

    // Act
    json.setAttestationInstance(json1);

    // Assert
    assertSame(json1, json.attestationInstance);
  }
  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutableAttestationInstanceRecipient.Json json = new ImmutableAttestationInstanceRecipient.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

