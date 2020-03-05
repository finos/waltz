package com.khartec.waltz.model.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableActorDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableActor.Json actualJson = new ImmutableActor.Json();

    // Assert
    assertNull(actualJson.lastUpdatedBy);
    assertFalse(actualJson.isExternalIsSet);
    assertNull(actualJson.name);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.isExternal);
    assertNull(actualJson.description);
    assertNull(actualJson.kind);
    assertNull(actualJson.lastUpdatedAt);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableActor.Json json = new ImmutableActor.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIsExternalTest() {
    // Arrange
    ImmutableActor.Json json = new ImmutableActor.Json();

    // Act
    json.setIsExternal(true);

    // Assert
    assertTrue(json.isExternalIsSet);
    assertTrue(json.isExternal);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableActor.Json json = new ImmutableActor.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableActor.Json json = new ImmutableActor.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableActor.Json json = new ImmutableActor.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableActor.Json json = new ImmutableActor.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

