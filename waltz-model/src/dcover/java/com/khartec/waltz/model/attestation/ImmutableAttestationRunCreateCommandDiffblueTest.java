package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableAttestationRunCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableAttestationRunCreateCommand.Json()).involvementKindIds.size());
  }
  @Test
  public void setAttestedEntityKindTest() {
    // Arrange
    ImmutableAttestationRunCreateCommand.Json json = new ImmutableAttestationRunCreateCommand.Json();

    // Act
    json.setAttestedEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.attestedEntityKind);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAttestationRunCreateCommand.Json json = new ImmutableAttestationRunCreateCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setInvolvementKindIdsTest() {
    // Arrange
    ImmutableAttestationRunCreateCommand.Json json = new ImmutableAttestationRunCreateCommand.Json();
    HashSet<Long> resultLongSet = new HashSet<Long>();
    resultLongSet.add(1L);

    // Act
    json.setInvolvementKindIds(resultLongSet);

    // Assert
    assertSame(resultLongSet, json.involvementKindIds);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAttestationRunCreateCommand.Json json = new ImmutableAttestationRunCreateCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setTargetEntityKindTest() {
    // Arrange
    ImmutableAttestationRunCreateCommand.Json json = new ImmutableAttestationRunCreateCommand.Json();

    // Act
    json.setTargetEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.targetEntityKind);
  }
}

