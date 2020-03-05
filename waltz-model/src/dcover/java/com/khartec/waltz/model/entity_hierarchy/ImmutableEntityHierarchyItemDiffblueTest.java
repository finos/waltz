package com.khartec.waltz.model.entity_hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LevelProvider;
import com.khartec.waltz.model.ParentIdProvider;
import org.junit.Test;

public class ImmutableEntityHierarchyItemDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityHierarchyItem.Json actualJson = new ImmutableEntityHierarchyItem.Json();

    // Assert
    assertSame(actualJson.id, actualJson.parentId);
  }





  @Test
  public void setKindTest() {
    // Arrange
    ImmutableEntityHierarchyItem.Json json = new ImmutableEntityHierarchyItem.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }

  @Test
  public void setLevelTest() {
    // Arrange
    ImmutableEntityHierarchyItem.Json json = new ImmutableEntityHierarchyItem.Json();

    // Act
    json.setLevel(1);

    // Assert
    assertEquals(1, json.level);
    assertTrue(json.levelIsSet);
  }
}

