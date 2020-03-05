package com.khartec.waltz.model.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CodeProvider;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ParentIdProvider;
import org.junit.Test;

public class ImmutableDataTypeDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDataType.Json actualJson = new ImmutableDataType.Json();

    // Assert
    assertSame(actualJson.id, actualJson.parentId);
  }
  @Test
  public void setCodeTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setCode("code");

    // Assert
    assertEquals("code", json.code);
  }
  @Test
  public void setConcreteTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setConcrete(true);

    // Assert
    assertTrue(json.concrete);
    assertTrue(json.concreteIsSet);
  }
  @Test
  public void setDeprecatedTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setDeprecated(true);

    // Assert
    assertTrue(json.deprecatedIsSet);
    assertTrue(json.deprecated);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setUnknownTest() {
    // Arrange
    ImmutableDataType.Json json = new ImmutableDataType.Json();

    // Act
    json.setUnknown(true);

    // Assert
    assertTrue(json.unknown);
    assertTrue(json.unknownIsSet);
  }
}

