package com.khartec.waltz.model.change_unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableChangeUnitDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableChangeUnit.Json actualJson = new ImmutableChangeUnit.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(actualJson.externalId, optional);
    assertSame(optional, actualJson.changeSetId);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setActionTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setAction(ChangeAction.ACTIVATE);

    // Assert
    assertEquals(ChangeAction.ACTIVATE, json.action);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setExecutionStatusTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setExecutionStatus(ExecutionStatus.PENDING);

    // Assert
    assertEquals(ExecutionStatus.PENDING, json.executionStatus);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setSubjectInitialStatusTest() {
    // Arrange
    ImmutableChangeUnit.Json json = new ImmutableChangeUnit.Json();

    // Act
    json.setSubjectInitialStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.subjectInitialStatus);
  }
}

