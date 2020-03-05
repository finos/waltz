package com.khartec.waltz.model.involvement_kind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableInvolvementKindCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableInvolvementKindCreateCommand.Json actualJson = new ImmutableInvolvementKindCreateCommand.Json();

    // Assert
    assertNull(actualJson.name);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableInvolvementKindCreateCommand.Json json = new ImmutableInvolvementKindCreateCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableInvolvementKindCreateCommand.Json json = new ImmutableInvolvementKindCreateCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

