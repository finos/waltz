package com.khartec.waltz.model.assessment_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableAssessmentDefinitionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAssessmentDefinition.Json actualJson = new ImmutableAssessmentDefinition.Json();

    // Assert
    Optional<String> optional = actualJson.externalId;
    assertSame(actualJson.permittedRole, optional);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.permittedRole);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityKindTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.entityKind);
  }
  @Test
  public void setIsReadOnlyTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setIsReadOnly(true);

    // Assert
    assertTrue(json.isReadOnlyIsSet);
    assertTrue(json.isReadOnly);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setRatingSchemeIdTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setRatingSchemeId(123L);

    // Assert
    assertTrue(json.ratingSchemeIdIsSet);
    assertEquals(123L, json.ratingSchemeId);
  }
  @Test
  public void setVisibilityTest() {
    // Arrange
    ImmutableAssessmentDefinition.Json json = new ImmutableAssessmentDefinition.Json();

    // Act
    json.setVisibility(AssessmentVisibility.PRIMARY);

    // Assert
    assertEquals(AssessmentVisibility.PRIMARY, json.visibility);
  }
}

