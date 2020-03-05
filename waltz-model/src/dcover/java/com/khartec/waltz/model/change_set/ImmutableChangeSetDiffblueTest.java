package com.khartec.waltz.model.change_set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableChangeSetDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableChangeSet.Json actualJson = new ImmutableChangeSet.Json();

    // Assert
    Optional<EntityReference> optional = actualJson.parentEntity;
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.plannedDate);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableChangeSet.Json json = new ImmutableChangeSet.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableChangeSet.Json json = new ImmutableChangeSet.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableChangeSet.Json json = new ImmutableChangeSet.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableChangeSet.Json json = new ImmutableChangeSet.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableChangeSet.Json json = new ImmutableChangeSet.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableChangeSet.Json json = new ImmutableChangeSet.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

