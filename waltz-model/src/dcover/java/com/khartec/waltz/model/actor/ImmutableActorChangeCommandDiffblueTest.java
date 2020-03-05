package com.khartec.waltz.model.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.command.EntityChangeCommand;
import com.khartec.waltz.model.command.FieldChange;
import java.util.Optional;
import org.junit.Test;

public class ImmutableActorChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableActorChangeCommand.Json actualJson = new ImmutableActorChangeCommand.Json();

    // Assert
    Optional<FieldChange<Boolean>> optional = actualJson.isExternal;
    assertSame(actualJson.description, optional);
    assertSame(optional, actualJson.lastUpdate);
    assertSame(optional, actualJson.name);
    assertSame(optional, actualJson.description);
  }
  @Test
  public void setIdTest() {
    // Arrange
    ImmutableActorChangeCommand.Json json = new ImmutableActorChangeCommand.Json();

    // Act
    json.setId(123L);

    // Assert
    assertEquals(123L, json.id);
    assertTrue(json.idIsSet);
  }
}

