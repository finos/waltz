package com.khartec.waltz.model.flow_diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableSaveDiagramCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSaveDiagramCommand.Json actualJson = new ImmutableSaveDiagramCommand.Json();

    // Assert
    List<FlowDiagramAnnotation> actualFlowDiagramAnnotationList = actualJson.annotations;
    List<FlowDiagramEntity> flowDiagramEntityList = actualJson.entities;
    assertEquals(0, flowDiagramEntityList.size());
    assertSame(flowDiagramEntityList, actualFlowDiagramAnnotationList);
  }
  @Test
  public void setAnnotationsTest() {
    // Arrange
    ImmutableSaveDiagramCommand.Json json = new ImmutableSaveDiagramCommand.Json();
    ArrayList<FlowDiagramAnnotation> flowDiagramAnnotationList = new ArrayList<FlowDiagramAnnotation>();
    flowDiagramAnnotationList.add(new ImmutableFlowDiagramAnnotation.Json());

    // Act
    json.setAnnotations(flowDiagramAnnotationList);

    // Assert
    assertSame(flowDiagramAnnotationList, json.annotations);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSaveDiagramCommand.Json json = new ImmutableSaveDiagramCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntitiesTest() {
    // Arrange
    ImmutableSaveDiagramCommand.Json json = new ImmutableSaveDiagramCommand.Json();
    ArrayList<FlowDiagramEntity> flowDiagramEntityList = new ArrayList<FlowDiagramEntity>();
    flowDiagramEntityList.add(new ImmutableFlowDiagramEntity.Json());

    // Act
    json.setEntities(flowDiagramEntityList);

    // Assert
    assertSame(flowDiagramEntityList, json.entities);
  }
  @Test
  public void setLayoutDataTest() {
    // Arrange
    ImmutableSaveDiagramCommand.Json json = new ImmutableSaveDiagramCommand.Json();

    // Act
    json.setLayoutData("layoutData");

    // Assert
    assertEquals("layoutData", json.layoutData);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSaveDiagramCommand.Json json = new ImmutableSaveDiagramCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

