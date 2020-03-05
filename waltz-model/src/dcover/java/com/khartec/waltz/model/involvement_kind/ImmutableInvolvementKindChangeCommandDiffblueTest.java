package com.khartec.waltz.model.involvement_kind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.command.EntityChangeCommand;
import com.khartec.waltz.model.command.FieldChange;
import java.util.Optional;
import org.junit.Test;

public class ImmutableInvolvementKindChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableInvolvementKindChangeCommand.Json actualJson = new ImmutableInvolvementKindChangeCommand.Json();

    // Assert
    Optional<FieldChange<String>> optional = actualJson.description;
    assertSame(actualJson.lastUpdate, optional);
    assertSame(optional, actualJson.name);
    assertSame(optional, actualJson.lastUpdate);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableInvolvementKindChangeCommand.Json json = new ImmutableInvolvementKindChangeCommand.Json();

    // Act
    json.setId(123L);

    // Assert
    assertTrue(json.idIsSet);
    assertEquals(123L, json.id);
  }
}

