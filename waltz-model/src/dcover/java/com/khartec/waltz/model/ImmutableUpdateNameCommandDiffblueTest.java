package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableUpdateNameCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableUpdateNameCommand.Json()).newName);
  }
  @Test
  public void setNewNameTest() {
    // Arrange
    ImmutableUpdateNameCommand.Json json = new ImmutableUpdateNameCommand.Json();

    // Act
    json.setNewName("newName");

    // Assert
    assertEquals("newName", json.newName);
  }
}

