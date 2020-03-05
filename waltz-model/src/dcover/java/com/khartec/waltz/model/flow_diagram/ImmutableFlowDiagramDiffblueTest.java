package com.khartec.waltz.model.flow_diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.IsRemovedProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableFlowDiagramDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableFlowDiagram.Json actualJson = new ImmutableFlowDiagram.Json();

    // Assert
    assertNull(actualJson.kind);
    assertNull(actualJson.description);
    assertNull(actualJson.name);
    assertNull(actualJson.layoutData);
    assertFalse(actualJson.isRemovedIsSet);
    assertFalse(actualJson.isRemoved);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.lastUpdatedAt);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableFlowDiagram.Json json = new ImmutableFlowDiagram.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIsRemovedTest() {
    // Arrange
    ImmutableFlowDiagram.Json json = new ImmutableFlowDiagram.Json();

    // Act
    json.setIsRemoved(true);

    // Assert
    assertTrue(json.isRemovedIsSet);
    assertTrue(json.isRemoved);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableFlowDiagram.Json json = new ImmutableFlowDiagram.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableFlowDiagram.Json json = new ImmutableFlowDiagram.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setLayoutDataTest() {
    // Arrange
    ImmutableFlowDiagram.Json json = new ImmutableFlowDiagram.Json();

    // Act
    json.setLayoutData("layoutData");

    // Assert
    assertEquals("layoutData", json.layoutData);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableFlowDiagram.Json json = new ImmutableFlowDiagram.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

