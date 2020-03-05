package com.khartec.waltz.model.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class ImmutableApplicationIdSelectionOptionsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableApplicationIdSelectionOptions.Json actualJson = new ImmutableApplicationIdSelectionOptions.Json();

    // Assert
    Set<ApplicationKind> actualApplicationKindSet = actualJson.applicationKinds;
    Set<EntityLifecycleStatus> entityLifecycleStatusSet = actualJson.entityLifecycleStatuses;
    assertEquals(0, entityLifecycleStatusSet.size());
    assertSame(entityLifecycleStatusSet, actualApplicationKindSet);
  }
  @Test
  public void setApplicationKindsTest() {
    // Arrange
    ImmutableApplicationIdSelectionOptions.Json json = new ImmutableApplicationIdSelectionOptions.Json();
    HashSet<ApplicationKind> applicationKindSet = new HashSet<ApplicationKind>();
    applicationKindSet.add(ApplicationKind.IN_HOUSE);

    // Act
    json.setApplicationKinds(applicationKindSet);

    // Assert
    assertTrue(json.applicationKindsIsSet);
    assertSame(applicationKindSet, json.applicationKinds);
  }
  @Test
  public void setEntityLifecycleStatusesTest() {
    // Arrange
    ImmutableApplicationIdSelectionOptions.Json json = new ImmutableApplicationIdSelectionOptions.Json();
    HashSet<EntityLifecycleStatus> entityLifecycleStatusSet = new HashSet<EntityLifecycleStatus>();
    entityLifecycleStatusSet.add(EntityLifecycleStatus.ACTIVE);

    // Act
    json.setEntityLifecycleStatuses(entityLifecycleStatusSet);

    // Assert
    assertTrue(json.entityLifecycleStatusesIsSet);
    assertSame(entityLifecycleStatusSet, json.entityLifecycleStatuses);
  }
  @Test
  public void setScopeTest() {
    // Arrange
    ImmutableApplicationIdSelectionOptions.Json json = new ImmutableApplicationIdSelectionOptions.Json();

    // Act
    json.setScope(HierarchyQueryScope.EXACT);

    // Assert
    assertEquals(HierarchyQueryScope.EXACT, json.scope);
  }
}

