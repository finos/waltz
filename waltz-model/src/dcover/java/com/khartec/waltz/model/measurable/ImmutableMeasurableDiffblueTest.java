package com.khartec.waltz.model.measurable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.ExternalParentIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ParentIdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableMeasurableDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurable.Json actualJson = new ImmutableMeasurable.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(optional, actualJson.externalId);
    assertSame(optional, actualJson.externalParentId);
    assertSame(optional, actualJson.parentId);
  }
  @Test
  public void setCategoryIdTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setCategoryId(123L);

    // Assert
    assertTrue(json.categoryIdIsSet);
    assertEquals(123L, json.categoryId);
  }
  @Test
  public void setConcreteTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setConcrete(true);

    // Assert
    assertTrue(json.concreteIsSet);
    assertTrue(json.concrete);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableMeasurable.Json json = new ImmutableMeasurable.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

