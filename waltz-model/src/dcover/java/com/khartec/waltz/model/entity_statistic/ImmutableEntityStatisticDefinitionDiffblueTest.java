package com.khartec.waltz.model.entity_statistic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ParentIdProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityStatisticDefinitionDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityStatisticDefinition.Json actualJson = new ImmutableEntityStatisticDefinition.Json();

    // Assert
    assertSame(actualJson.id, actualJson.parentId);
  }







  @Test
  public void setActiveTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setActive(true);

    // Assert
    assertTrue(json.active);
    assertTrue(json.activeIsSet);
  }

  @Test
  public void setCategoryTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setCategory(StatisticCategory.COMPLIANCE);

    // Assert
    assertEquals(StatisticCategory.COMPLIANCE, json.category);
  }

  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setEntityVisibilityTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setEntityVisibility(true);

    // Assert
    assertEquals(Boolean.valueOf(true), json.entityVisibility);
  }

  @Test
  public void setHistoricRendererTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setHistoricRenderer("historicRenderer");

    // Assert
    assertEquals("historicRenderer", json.historicRenderer);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setRendererTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setRenderer("renderer");

    // Assert
    assertEquals("renderer", json.renderer);
  }

  @Test
  public void setRollupKindTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setRollupKind(RollupKind.COUNT_BY_ENTITY);

    // Assert
    assertEquals(RollupKind.COUNT_BY_ENTITY, json.rollupKind);
  }

  @Test
  public void setRollupVisibilityTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setRollupVisibility(true);

    // Assert
    assertEquals(Boolean.valueOf(true), json.rollupVisibility);
  }

  @Test
  public void setTypeTest() {
    // Arrange
    ImmutableEntityStatisticDefinition.Json json = new ImmutableEntityStatisticDefinition.Json();

    // Act
    json.setType(StatisticType.BOOLEAN);

    // Assert
    assertEquals(StatisticType.BOOLEAN, json.type);
  }
}

