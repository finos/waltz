package com.khartec.waltz.model.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.IsRemovedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.OrganisationalUnitIdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.rating.RagRating;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Test;

public class ImmutableApplicationDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableApplication.Json actualJson = new ImmutableApplication.Json();

    // Assert
    Optional<LocalDateTime> optional = actualJson.plannedRetirementDate;
    assertSame(optional, actualJson.parentAssetCode);
    assertSame(optional, actualJson.assetCode);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.actualRetirementDate);
  }
  @Test
  public void setApplicationKindTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setApplicationKind(ApplicationKind.IN_HOUSE);

    // Assert
    assertEquals(ApplicationKind.IN_HOUSE, json.applicationKind);
  }
  @Test
  public void setBusinessCriticalityTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setBusinessCriticality(Criticality.LOW);

    // Assert
    assertEquals(Criticality.LOW, json.businessCriticality);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setIsRemovedTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setIsRemoved(true);

    // Assert
    assertTrue(json.isRemoved);
    assertTrue(json.isRemovedIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLifecyclePhaseTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setLifecyclePhase(LifecyclePhase.PRODUCTION);

    // Assert
    assertEquals(LifecyclePhase.PRODUCTION, json.lifecyclePhase);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setOrganisationalUnitIdTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setOrganisationalUnitId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.organisationalUnitId);
  }
  @Test
  public void setOverallRatingTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setOverallRating(RagRating.R);

    // Assert
    assertEquals(RagRating.R, json.overallRating);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableApplication.Json json = new ImmutableApplication.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

