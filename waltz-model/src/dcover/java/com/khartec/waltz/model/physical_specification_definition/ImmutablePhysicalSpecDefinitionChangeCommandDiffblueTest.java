package com.khartec.waltz.model.physical_specification_definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import java.util.Optional;
import org.junit.Test;

public class ImmutablePhysicalSpecDefinitionChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecDefinitionChangeCommand.Json actualJson = new ImmutablePhysicalSpecDefinitionChangeCommand.Json();

    // Assert
    Optional<String> optional = actualJson.delimiter;
    assertSame(actualJson.id, optional);
    assertSame(optional, actualJson.id);
  }
  @Test
  public void setStatusTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionChangeCommand.Json();

    // Act
    json.setStatus(ReleaseLifecycleStatus.DRAFT);

    // Assert
    assertEquals(ReleaseLifecycleStatus.DRAFT, json.status);
  }
  @Test
  public void setTypeTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionChangeCommand.Json();

    // Act
    json.setType(PhysicalSpecDefinitionType.DELIMITED);

    // Assert
    assertEquals(PhysicalSpecDefinitionType.DELIMITED, json.type);
  }
  @Test
  public void setVersionTest() {
    // Arrange
    ImmutablePhysicalSpecDefinitionChangeCommand.Json json = new ImmutablePhysicalSpecDefinitionChangeCommand.Json();

    // Act
    json.setVersion("version");

    // Assert
    assertEquals("version", json.version);
  }
}

