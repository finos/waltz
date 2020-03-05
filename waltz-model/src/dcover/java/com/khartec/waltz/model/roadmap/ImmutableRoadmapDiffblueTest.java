package com.khartec.waltz.model.roadmap;

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
import org.junit.Test;

public class ImmutableRoadmapDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableRoadmap.Json actualJson = new ImmutableRoadmap.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.entityLifecycleStatus);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.columnType);
    assertNull(actualJson.description);
    assertNull(actualJson.name);
    assertFalse(actualJson.ratingSchemeIdIsSet);
    assertEquals(0L, actualJson.ratingSchemeId);
    assertNull(actualJson.rowType);
  }







  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableRoadmap.Json json = new ImmutableRoadmap.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableRoadmap.Json json = new ImmutableRoadmap.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableRoadmap.Json json = new ImmutableRoadmap.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableRoadmap.Json json = new ImmutableRoadmap.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setRatingSchemeIdTest() {
    // Arrange
    ImmutableRoadmap.Json json = new ImmutableRoadmap.Json();

    // Act
    json.setRatingSchemeId(123L);

    // Assert
    assertTrue(json.ratingSchemeIdIsSet);
    assertEquals(123L, json.ratingSchemeId);
  }
}

