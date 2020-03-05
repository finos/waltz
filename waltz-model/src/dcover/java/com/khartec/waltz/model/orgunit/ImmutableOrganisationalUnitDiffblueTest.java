package com.khartec.waltz.model.orgunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ParentIdProvider;
import org.junit.Test;

public class ImmutableOrganisationalUnitDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableOrganisationalUnit.Json actualJson = new ImmutableOrganisationalUnit.Json();

    // Assert
    assertSame(actualJson.parentId, actualJson.id);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableOrganisationalUnit.Json json = new ImmutableOrganisationalUnit.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableOrganisationalUnit.Json json = new ImmutableOrganisationalUnit.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableOrganisationalUnit.Json json = new ImmutableOrganisationalUnit.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

