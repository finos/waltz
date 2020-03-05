package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableAttestationInstanceDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttestationInstance.Json actualJson = new ImmutableAttestationInstance.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(optional, actualJson.attestedBy);
    assertSame(optional, actualJson.attestedAt);
  }



  @Test
  public void setAttestationRunIdTest() {
    // Arrange
    ImmutableAttestationInstance.Json json = new ImmutableAttestationInstance.Json();

    // Act
    json.setAttestationRunId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.attestationRunId);
  }

  @Test
  public void setAttestedEntityKindTest() {
    // Arrange
    ImmutableAttestationInstance.Json json = new ImmutableAttestationInstance.Json();

    // Act
    json.setAttestedEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.attestedEntityKind);
  }
}

