package com.khartec.waltz.model.enduserapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.application.LifecyclePhase;
import org.junit.Test;

public class ImmutableEndUserApplicationDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEndUserApplication.Json actualJson = new ImmutableEndUserApplication.Json();

    // Assert
    assertSame(actualJson.id, actualJson.externalId);
  }








  @Test
  public void setApplicationKindTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setApplicationKind("applicationKind");

    // Assert
    assertEquals("applicationKind", json.applicationKind);
  }

  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setIsPromotedTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setIsPromoted(true);

    // Assert
    assertEquals(Boolean.valueOf(true), json.isPromoted);
  }

  @Test
  public void setKindTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }

  @Test
  public void setLifecyclePhaseTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setLifecyclePhase(LifecyclePhase.PRODUCTION);

    // Assert
    assertEquals(LifecyclePhase.PRODUCTION, json.lifecyclePhase);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setOrganisationalUnitIdTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setOrganisationalUnitId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.organisationalUnitId);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setRiskRatingTest() {
    // Arrange
    ImmutableEndUserApplication.Json json = new ImmutableEndUserApplication.Json();

    // Act
    json.setRiskRating(Criticality.LOW);

    // Assert
    assertEquals(Criticality.LOW, json.riskRating);
  }
}

