package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HierarchyQueryScopeDiffblueTest {
  @Test
  public void determineDownwardsScopeForKindTest() {
    // Arrange
    HierarchyQueryScope actualDetermineDownwardsScopeForKindResult = HierarchyQueryScope
        .determineDownwardsScopeForKind(EntityKind.ACTOR);

    // Act and Assert
    assertEquals(HierarchyQueryScope.EXACT, actualDetermineDownwardsScopeForKindResult);
    assertEquals(HierarchyQueryScope.CHILDREN,
        HierarchyQueryScope.determineDownwardsScopeForKind(EntityKind.CHANGE_INITIATIVE));
  }

  @Test
  public void determineUpwardsScopeForKindTest() {
    // Arrange
    HierarchyQueryScope actualDetermineUpwardsScopeForKindResult = HierarchyQueryScope
        .determineUpwardsScopeForKind(EntityKind.ACTOR);

    // Act and Assert
    assertEquals(HierarchyQueryScope.EXACT, actualDetermineUpwardsScopeForKindResult);
    assertEquals(HierarchyQueryScope.PARENTS,
        HierarchyQueryScope.determineUpwardsScopeForKind(EntityKind.CHANGE_INITIATIVE));
  }
}

