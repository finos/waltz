package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableIdSelectionOptionsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableIdSelectionOptions.Json()).entityLifecycleStatuses.size());
  }
  @Test
  public void setEntityLifecycleStatusesTest() {
    // Arrange
    ImmutableIdSelectionOptions.Json json = new ImmutableIdSelectionOptions.Json();
    HashSet<EntityLifecycleStatus> entityLifecycleStatusSet = new HashSet<EntityLifecycleStatus>();
    entityLifecycleStatusSet.add(EntityLifecycleStatus.ACTIVE);

    // Act
    json.setEntityLifecycleStatuses(entityLifecycleStatusSet);

    // Assert
    assertSame(entityLifecycleStatusSet, json.entityLifecycleStatuses);
    assertTrue(json.entityLifecycleStatusesIsSet);
  }
  @Test
  public void setEntityReferenceTest() {
    // Arrange
    ImmutableIdSelectionOptions.Json json = new ImmutableIdSelectionOptions.Json();
    ImmutableEntityReference.Json json1 = new ImmutableEntityReference.Json();

    // Act
    json.setEntityReference(json1);

    // Assert
    assertSame(json1, json.entityReference);
  }
  @Test
  public void setFiltersTest() {
    // Arrange
    ImmutableIdSelectionOptions.Json json = new ImmutableIdSelectionOptions.Json();
    ImmutableSelectionFilters.Json json1 = new ImmutableSelectionFilters.Json();

    // Act
    json.setFilters(json1);

    // Assert
    assertSame(json1, json.filters);
  }
  @Test
  public void setScopeTest() {
    // Arrange
    ImmutableIdSelectionOptions.Json json = new ImmutableIdSelectionOptions.Json();

    // Act
    json.setScope(HierarchyQueryScope.EXACT);

    // Assert
    assertEquals(HierarchyQueryScope.EXACT, json.scope);
  }
}

