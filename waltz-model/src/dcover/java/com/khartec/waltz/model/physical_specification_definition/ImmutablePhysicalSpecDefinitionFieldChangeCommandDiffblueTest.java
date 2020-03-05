package com.khartec.waltz.model.physical_specification_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.FieldDataType;
import com.khartec.waltz.model.NameProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutablePhysicalSpecDefinitionFieldChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json actualJson = new ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(actualJson.logicalDataElementId, optional);
    assertSame(optional, actualJson.logicalDataElementId);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setPositionTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json();

    // Act
    json.setPosition(1);

    // Assert
    assertEquals(1, json.position);
    assertTrue(json.positionIsSet);
  }
  @Test
  public void setTypeTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionFieldChangeCommand.Json();

    // Act
    json.setType(FieldDataType.DATE);

    // Assert
    assertEquals(FieldDataType.DATE, json.type);
  }
}

