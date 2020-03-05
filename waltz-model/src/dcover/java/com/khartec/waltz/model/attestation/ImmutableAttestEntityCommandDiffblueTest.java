package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.EntityKind;
import org.junit.Test;

public class ImmutableAttestEntityCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttestEntityCommand.Json actualJson = new ImmutableAttestEntityCommand.Json();

    // Assert
    assertNull(actualJson.attestedEntityKind);
    assertNull(actualJson.entityReference);
  }
  @Test
  public void setAttestedEntityKindTest() {
    // Arrange
    ImmutableAttestEntityCommand.Json json = new ImmutableAttestEntityCommand.Json();

    // Act
    json.setAttestedEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.attestedEntityKind);
  }
}

