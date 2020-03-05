package com.khartec.waltz.model.change_initiative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.OrganisationalUnitIdProvider;
import com.khartec.waltz.model.ParentIdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.application.LifecyclePhase;
import java.util.Date;
import java.util.Optional;
import org.junit.Test;

public class ImmutableChangeInitiativeDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableChangeInitiative.Json actualJson = new ImmutableChangeInitiative.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(optional, actualJson.parentId);
    assertSame(optional, actualJson.lastUpdate);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setChangeInitiativeKindTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setChangeInitiativeKind(ChangeInitiativeKind.INITIATIVE);

    // Assert
    assertEquals(ChangeInitiativeKind.INITIATIVE, json.changeInitiativeKind);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEndDateTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setEndDate(date);

    // Assert
    assertSame(date, json.endDate);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLifecyclePhaseTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setLifecyclePhase(LifecyclePhase.PRODUCTION);

    // Assert
    assertEquals(LifecyclePhase.PRODUCTION, json.lifecyclePhase);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setOrganisationalUnitIdTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setOrganisationalUnitId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.organisationalUnitId);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setStartDateTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableChangeInitiative.Json json = new ImmutableChangeInitiative.Json();

    // Act
    json.setStartDate(date);

    // Assert
    assertSame(date, json.startDate);
  }
}

