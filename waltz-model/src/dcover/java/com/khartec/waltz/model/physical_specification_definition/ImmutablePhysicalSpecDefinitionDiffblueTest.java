package com.khartec.waltz.model.physical_specification_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import org.junit.Test;

public class ImmutablePhysicalSpecDefinitionDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecDefinition.Json actualJson = new ImmutablePhysicalSpecDefinition.Json();

    // Assert
    assertSame(actualJson.id, actualJson.delimiter);
  }






  @Test
  public void setCreatedByTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setCreatedBy("createdBy");

    // Assert
    assertEquals("createdBy", json.createdBy);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setSpecificationIdTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setSpecificationId(123L);

    // Assert
    assertTrue(json.specificationIdIsSet);
    assertEquals(123L, json.specificationId);
  }

  @Test
  public void setStatusTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setStatus(ReleaseLifecycleStatus.DRAFT);

    // Assert
    assertEquals(ReleaseLifecycleStatus.DRAFT, json.status);
  }

  @Test
  public void setTypeTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setType(PhysicalSpecDefinitionType.DELIMITED);

    // Assert
    assertEquals(PhysicalSpecDefinitionType.DELIMITED, json.type);
  }

  @Test
  public void setVersionTest() {
    // Arrange
    ImmutablePhysicalSpecDefinition.Json json = new ImmutablePhysicalSpecDefinition.Json();

    // Act
    json.setVersion("version");

    // Assert
    assertEquals("version", json.version);
  }
}

