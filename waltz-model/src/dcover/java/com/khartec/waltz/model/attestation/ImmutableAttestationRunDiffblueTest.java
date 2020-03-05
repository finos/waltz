package com.khartec.waltz.model.attestation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.WaltzEntity;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableAttestationRunDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttestationRun.Json actualJson = new ImmutableAttestationRun.Json();

    // Assert
    assertSame(actualJson.id, actualJson.attestedEntityRef);
    assertEquals(0, actualJson.involvementKindIds.size());
  }
  @Test
  public void setAttestedEntityKindTest() {
    // Arrange
    ImmutableAttestationRun.Json json = new ImmutableAttestationRun.Json();

    // Act
    json.setAttestedEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.attestedEntityKind);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAttestationRun.Json json = new ImmutableAttestationRun.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setInvolvementKindIdsTest() {
    // Arrange
    ImmutableAttestationRun.Json json = new ImmutableAttestationRun.Json();
    HashSet<Long> resultLongSet = new HashSet<Long>();
    resultLongSet.add(1L);

    // Act
    json.setInvolvementKindIds(resultLongSet);

    // Assert
    assertSame(resultLongSet, json.involvementKindIds);
  }
  @Test
  public void setIssuedByTest() {
    // Arrange
    ImmutableAttestationRun.Json json = new ImmutableAttestationRun.Json();

    // Act
    json.setIssuedBy("issuedBy");

    // Assert
    assertEquals("issuedBy", json.issuedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAttestationRun.Json json = new ImmutableAttestationRun.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setTargetEntityKindTest() {
    // Arrange
    ImmutableAttestationRun.Json json = new ImmutableAttestationRun.Json();

    // Act
    json.setTargetEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.targetEntityKind);
  }
}

