package com.khartec.waltz.model.flow_diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableFlowDiagramAnnotationDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableFlowDiagramAnnotation.Json actualJson = new ImmutableFlowDiagramAnnotation.Json();

    // Assert
    assertNull(actualJson.annotationId);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.note);
  }


  @Test
  public void setAnnotationIdTest() {
    // Arrange
    ImmutableFlowDiagramAnnotation.Json json = new ImmutableFlowDiagramAnnotation.Json();

    // Act
    json.setAnnotationId("123");

    // Assert
    assertEquals("123", json.annotationId);
  }

  @Test
  public void setNoteTest() {
    // Arrange
    ImmutableFlowDiagramAnnotation.Json json = new ImmutableFlowDiagramAnnotation.Json();

    // Act
    json.setNote("note");

    // Assert
    assertEquals("note", json.note);
  }
}

