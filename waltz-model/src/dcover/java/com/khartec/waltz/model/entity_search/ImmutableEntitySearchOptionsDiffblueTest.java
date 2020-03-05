package com.khartec.waltz.model.entity_search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableEntitySearchOptionsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntitySearchOptions.Json actualJson = new ImmutableEntitySearchOptions.Json();

    // Assert
    List<EntityKind> actualEntityKindList = actualJson.entityKinds;
    List<EntityLifecycleStatus> entityLifecycleStatusList = actualJson.entityLifecycleStatuses;
    assertEquals(0, entityLifecycleStatusList.size());
    assertSame(entityLifecycleStatusList, actualEntityKindList);
  }
  @Test
  public void setEntityKindsTest() {
    // Arrange
    ImmutableEntitySearchOptions.Json json = new ImmutableEntitySearchOptions.Json();
    ArrayList<EntityKind> entityKindList = new ArrayList<EntityKind>();
    entityKindList.add(EntityKind.ACTOR);

    // Act
    json.setEntityKinds(entityKindList);

    // Assert
    assertSame(entityKindList, json.entityKinds);
  }
  @Test
  public void setEntityLifecycleStatusesTest() {
    // Arrange
    ImmutableEntitySearchOptions.Json json = new ImmutableEntitySearchOptions.Json();
    ArrayList<EntityLifecycleStatus> entityLifecycleStatusList = new ArrayList<EntityLifecycleStatus>();
    entityLifecycleStatusList.add(EntityLifecycleStatus.ACTIVE);

    // Act
    json.setEntityLifecycleStatuses(entityLifecycleStatusList);

    // Assert
    assertTrue(json.entityLifecycleStatusesIsSet);
    assertSame(entityLifecycleStatusList, json.entityLifecycleStatuses);
  }
  @Test
  public void setLimitTest() {
    // Arrange
    ImmutableEntitySearchOptions.Json json = new ImmutableEntitySearchOptions.Json();

    // Act
    json.setLimit(1);

    // Assert
    assertTrue(json.limitIsSet);
    assertEquals(1, json.limit);
  }
  @Test
  public void setSearchQueryTest() {
    // Arrange
    ImmutableEntitySearchOptions.Json json = new ImmutableEntitySearchOptions.Json();

    // Act
    json.setSearchQuery("searchQuery");

    // Assert
    assertEquals("searchQuery", json.searchQuery);
  }
  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutableEntitySearchOptions.Json json = new ImmutableEntitySearchOptions.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

