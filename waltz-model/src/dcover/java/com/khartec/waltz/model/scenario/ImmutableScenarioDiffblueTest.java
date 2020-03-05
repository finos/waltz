package com.khartec.waltz.model.scenario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.PositionProvider;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import org.junit.Test;

public class ImmutableScenarioDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableScenario.Json actualJson = new ImmutableScenario.Json();

    // Assert
    assertFalse(actualJson.roadmapIdIsSet);
    assertNull(actualJson.description);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.scenarioType);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.releaseStatus);
    assertNull(actualJson.name);
    assertFalse(actualJson.positionIsSet);
    assertNull(actualJson.entityLifecycleStatus);
    assertEquals(0, actualJson.position);
    assertEquals(0L, actualJson.roadmapId);
    assertNull(actualJson.effectiveDate);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertTrue(json.positionIsSet);
    assertEquals(1, json.position);
  }
  @Test
  public void setReleaseStatusTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setReleaseStatus(ReleaseLifecycleStatus.DRAFT);

    // Assert
    assertEquals(ReleaseLifecycleStatus.DRAFT, json.releaseStatus);
  }
  @Test
  public void setRoadmapIdTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setRoadmapId(123L);

    // Assert
    assertTrue(json.roadmapIdIsSet);
    assertEquals(123L, json.roadmapId);
  }
  @Test
  public void setScenarioTypeTest() {
    // Arrange
    ImmutableScenario.Json json = new ImmutableScenario.Json();

    // Act
    json.setScenarioType(ScenarioType.TARGET);

    // Assert
    assertEquals(ScenarioType.TARGET, json.scenarioType);
  }
}

