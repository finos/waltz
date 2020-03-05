package com.khartec.waltz.model.drill_grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableDrillGridDefinitionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDrillGridDefinition.Json actualJson = new ImmutableDrillGridDefinition.Json();

    // Assert
    assertNull(actualJson.xAxis);
    assertNull(actualJson.name);
    assertNull(actualJson.yAxis);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableDrillGridDefinition.Json json = new ImmutableDrillGridDefinition.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableDrillGridDefinition.Json json = new ImmutableDrillGridDefinition.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

