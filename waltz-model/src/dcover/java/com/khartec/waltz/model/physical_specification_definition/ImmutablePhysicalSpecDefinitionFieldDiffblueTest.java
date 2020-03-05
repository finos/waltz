package com.khartec.waltz.model.physical_specification_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.FieldDataType;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutablePhysicalSpecDefinitionFieldDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecDefinitionField.Json actualJson = new ImmutablePhysicalSpecDefinitionField.Json();

    // Assert
    assertSame(actualJson.id, actualJson.logicalDataElementId);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionField.Json json = new ImmutablePhysicalSpecDefinitionField.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionField.Json json = new ImmutablePhysicalSpecDefinitionField.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionField.Json json = new ImmutablePhysicalSpecDefinitionField.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionField.Json json = new ImmutablePhysicalSpecDefinitionField.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertTrue(json.positionIsSet);
    assertEquals(1, json.position);
  }
  @Test
  public void setSpecDefinitionIdTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionField.Json json = new ImmutablePhysicalSpecDefinitionField.Json();

    // Act
    json.setSpecDefinitionId(123L);

    // Assert
    assertTrue(json.specDefinitionIdIsSet);
    assertEquals(123L, json.specDefinitionId);
  }
  @Test
  public void setTypeTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionField.Json json = new ImmutablePhysicalSpecDefinitionField.Json();

    // Act
    json.setType(FieldDataType.DATE);

    // Assert
    assertEquals(FieldDataType.DATE, json.type);
  }
}

