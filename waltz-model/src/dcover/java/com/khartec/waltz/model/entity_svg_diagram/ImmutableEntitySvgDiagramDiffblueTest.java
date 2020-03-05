package com.khartec.waltz.model.entity_svg_diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntitySvgDiagramDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntitySvgDiagram.Json actualJson = new ImmutableEntitySvgDiagram.Json();

    // Assert
    assertSame(actualJson.id, actualJson.externalId);
  }







  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntitySvgDiagram.Json json = new ImmutableEntitySvgDiagram.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEntitySvgDiagram.Json json = new ImmutableEntitySvgDiagram.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntitySvgDiagram.Json json = new ImmutableEntitySvgDiagram.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setSvgTest() {
    // Arrange
    ImmutableEntitySvgDiagram.Json json = new ImmutableEntitySvgDiagram.Json();

    // Act
    json.setSvg("svg");

    // Assert
    assertEquals("svg", json.svg);
  }
}

