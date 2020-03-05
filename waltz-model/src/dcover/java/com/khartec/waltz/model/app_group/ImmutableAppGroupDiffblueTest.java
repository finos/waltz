package com.khartec.waltz.model.app_group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.IsRemovedProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableAppGroupDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAppGroup.Json actualJson = new ImmutableAppGroup.Json();

    // Assert
    assertSame(actualJson.externalId, actualJson.id);
  }
  @Test
  public void setAppGroupKindTest() {
    // Arrange
    ImmutableAppGroup.Json json = new ImmutableAppGroup.Json();

    // Act
    json.setAppGroupKind(AppGroupKind.PUBLIC);

    // Assert
    assertEquals(AppGroupKind.PUBLIC, json.appGroupKind);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableAppGroup.Json json = new ImmutableAppGroup.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIsRemovedTest() {
    // Arrange
    ImmutableAppGroup.Json json = new ImmutableAppGroup.Json();

    // Act
    json.setIsRemoved(true);

    // Assert
    assertTrue(json.isRemoved);
    assertTrue(json.isRemovedIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableAppGroup.Json json = new ImmutableAppGroup.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAppGroup.Json json = new ImmutableAppGroup.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

