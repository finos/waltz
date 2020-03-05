package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Test;

public class ImmutableDateChangeCommandDiffblueTest {
    
  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("DateChangeCommand{}", ImmutableDateChangeCommand.builder().build().toString());
  }

  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    Optional<LocalDate> optional = (new ImmutableDateChangeCommand.Json()).newDateVal;
    assertEquals("Optional.empty", optional.toString());
    assertFalse(optional.isPresent());
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(
        ImmutableDateChangeCommand.fromJson(new ImmutableDateChangeCommand.Json()).equals("DateChangeCommand{"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("DateChangeCommand{}",
        ImmutableDateChangeCommand.fromJson(new ImmutableDateChangeCommand.Json()).toString());
  }


  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(177573, ImmutableDateChangeCommand.fromJson(new ImmutableDateChangeCommand.Json()).hashCode());
  }

  @Test
  public void newDateValTest() {
    // Arrange
    ImmutableDateChangeCommand.Json json = new ImmutableDateChangeCommand.Json();

    // Act and Assert
    assertSame(json.newDateVal, ImmutableDateChangeCommand.fromJson(json).newDateVal());
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("DateChangeCommand{}",
        ImmutableDateChangeCommand.fromJson(new ImmutableDateChangeCommand.Json()).toString());
  }
}

