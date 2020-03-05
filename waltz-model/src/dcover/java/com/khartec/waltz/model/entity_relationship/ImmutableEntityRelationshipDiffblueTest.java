package com.khartec.waltz.model.entity_relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityRelationshipDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityRelationship.Json actualJson = new ImmutableEntityRelationship.Json();

    // Assert
    assertNull(actualJson.relationship);
    assertNull(actualJson.b);
    assertNull(actualJson.provenance);
    assertNull(actualJson.a);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityRelationship.Json json = new ImmutableEntityRelationship.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableEntityRelationship.Json json = new ImmutableEntityRelationship.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityRelationship.Json json = new ImmutableEntityRelationship.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setRelationshipTest() {
    // Arrange
    ImmutableEntityRelationship.Json json = new ImmutableEntityRelationship.Json();

    // Act
    json.setRelationship(RelationshipKind.HAS);

    // Assert
    assertEquals(RelationshipKind.HAS, json.relationship);
  }
}

