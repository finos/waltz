package com.khartec.waltz.model.svg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableSvgDiagramDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSvgDiagram.Json actualJson = new ImmutableSvgDiagram.Json();

    // Assert
    assertNull(actualJson.displayWidthPercent);
    assertNull(actualJson.name);
    assertNull(actualJson.displayHeightPercent);
    assertNull(actualJson.description);
    assertEquals(0, actualJson.priority);
    assertNull(actualJson.svg);
    assertFalse(actualJson.priorityIsSet);
    assertNull(actualJson.group);
    assertNull(actualJson.product);
    assertNull(actualJson.keyProperty);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setDisplayHeightPercentTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setDisplayHeightPercent(1);

    // Assert
    assertEquals(Integer.valueOf(1), json.displayHeightPercent);
  }
  @Test
  public void setDisplayWidthPercentTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setDisplayWidthPercent(1);

    // Assert
    assertEquals(Integer.valueOf(1), json.displayWidthPercent);
  }
  @Test
  public void setGroupTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setGroup("group");

    // Assert
    assertEquals("group", json.group);
  }
  @Test
  public void setKeyPropertyTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setKeyProperty("keyProperty");

    // Assert
    assertEquals("keyProperty", json.keyProperty);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPriorityTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setPriority(1);

    // Assert
    assertEquals(1, json.priority);
    assertTrue(json.priorityIsSet);
  }
  @Test
  public void setProductTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setProduct("product");

    // Assert
    assertEquals("product", json.product);
  }
  @Test
  public void setSvgTest() {
    // Arrange
    ImmutableSvgDiagram.Json json = new ImmutableSvgDiagram.Json();

    // Act
    json.setSvg("svg");

    // Assert
    assertEquals("svg", json.svg);
  }
}

