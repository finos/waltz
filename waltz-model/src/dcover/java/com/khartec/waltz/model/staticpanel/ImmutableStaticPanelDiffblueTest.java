package com.khartec.waltz.model.staticpanel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.IdProvider;
import org.junit.Test;

public class ImmutableStaticPanelDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableStaticPanel.Json actualJson = new ImmutableStaticPanel.Json();

    // Assert
    assertFalse(actualJson.widthIsSet);
    assertFalse(actualJson.priorityIsSet);
    assertEquals(0, actualJson.width);
    assertNull(actualJson.title);
    assertNull(actualJson.encoding);
    assertNull(actualJson.icon);
    assertNull(actualJson.content);
    assertNull(actualJson.group);
    assertEquals(0, actualJson.priority);
  }
  @Test
  public void setContentTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setContent("content");

    // Assert
    assertEquals("content", json.content);
  }
  @Test
  public void setEncodingTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setEncoding(ContentKind.HTML);

    // Assert
    assertEquals(ContentKind.HTML, json.encoding);
  }
  @Test
  public void setGroupTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setGroup("group");

    // Assert
    assertEquals("group", json.group);
  }
  @Test
  public void setIconTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setIcon("icon");

    // Assert
    assertEquals("icon", json.icon);
  }
  @Test
  public void setPriorityTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setPriority(1);

    // Assert
    assertTrue(json.priorityIsSet);
    assertEquals(1, json.priority);
  }
  @Test
  public void setTitleTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setTitle("title");

    // Assert
    assertEquals("title", json.title);
  }
  @Test
  public void setWidthTest() {
    // Arrange
    ImmutableStaticPanel.Json json = new ImmutableStaticPanel.Json();

    // Act
    json.setWidth(1);

    // Assert
    assertTrue(json.widthIsSet);
    assertEquals(1, json.width);
  }
}

