package com.khartec.waltz.model.entity_named_note;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableEntityNamedNodeTypeDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableEntityNamedNodeType.Json()).applicableEntityKinds.size());
  }
  @Test
  public void setApplicableEntityKindsTest() {
    // Arrange
    ImmutableEntityNamedNodeType.Json json = new ImmutableEntityNamedNodeType.Json();
    HashSet<EntityKind> entityKindSet = new HashSet<EntityKind>();
    entityKindSet.add(EntityKind.ACTOR);

    // Act
    json.setApplicableEntityKinds(entityKindSet);

    // Assert
    assertSame(entityKindSet, json.applicableEntityKinds);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityNamedNodeType.Json json = new ImmutableEntityNamedNodeType.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIsReadOnlyTest() {
    // Arrange
    ImmutableEntityNamedNodeType.Json json = new ImmutableEntityNamedNodeType.Json();

    // Act
    json.setIsReadOnly(true);

    // Assert
    assertTrue(json.isReadOnly);
    assertTrue(json.isReadOnlyIsSet);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEntityNamedNodeType.Json json = new ImmutableEntityNamedNodeType.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutableEntityNamedNodeType.Json json = new ImmutableEntityNamedNodeType.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertTrue(json.positionIsSet);
    assertEquals(1, json.position);
  }
}

