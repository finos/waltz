package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableUpdateDescriptionCommandDiffblueTest {
    
  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("UpdateDescriptionCommand{newDescription=null}",
        ImmutableUpdateDescriptionCommand.builder().build().toString());
  }

  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableUpdateDescriptionCommand.Json()).newDescription);
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(
        ImmutableUpdateDescriptionCommand.fromJson(new ImmutableUpdateDescriptionCommand.Json()).equals("another"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("UpdateDescriptionCommand{newDescription=null}",
        ImmutableUpdateDescriptionCommand.fromJson(new ImmutableUpdateDescriptionCommand.Json()).toString());
  }


  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(177573,
        ImmutableUpdateDescriptionCommand.fromJson(new ImmutableUpdateDescriptionCommand.Json()).hashCode());
  }

  @Test
  public void newDescriptionTest2() {
    // Arrange, Act and Assert
    assertNull(
        ImmutableUpdateDescriptionCommand.fromJson(new ImmutableUpdateDescriptionCommand.Json()).newDescription());
  }

  @Test
  public void setNewDescriptionTest() {
    // Arrange
    ImmutableUpdateDescriptionCommand.Json json = new ImmutableUpdateDescriptionCommand.Json();

    // Act
    json.setNewDescription("newDescription");

    // Assert
    assertEquals("newDescription", json.newDescription);
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("UpdateDescriptionCommand{newDescription=null}",
        ImmutableUpdateDescriptionCommand.fromJson(new ImmutableUpdateDescriptionCommand.Json()).toString());
  }

  @Test
  public void withNewDescriptionTest() {
    // Arrange, Act and Assert
    assertEquals("UpdateDescriptionCommand{newDescription=value}", ImmutableUpdateDescriptionCommand
        .fromJson(new ImmutableUpdateDescriptionCommand.Json()).withNewDescription("value").toString());
  }
}

