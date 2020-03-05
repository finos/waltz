package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.util.Optional;
import org.junit.Test;

public class ImmutableIdCommandResponseDiffblueTest {
    
  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("IdCommandResponse{}", ImmutableIdCommandResponse.builder().build().toString());
  }

  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    Optional<Long> optional = (new ImmutableIdCommandResponse.Json()).id;
    assertEquals("Optional.empty", optional.toString());
    assertFalse(optional.isPresent());
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(
        ImmutableIdCommandResponse.fromJson(new ImmutableIdCommandResponse.Json()).equals("IdCommandResponse{"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("IdCommandResponse{}",
        ImmutableIdCommandResponse.fromJson(new ImmutableIdCommandResponse.Json()).toString());
  }



  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(177573, ImmutableIdCommandResponse.fromJson(new ImmutableIdCommandResponse.Json()).hashCode());
  }

  @Test
  public void idTest() {
    // Arrange
    ImmutableIdCommandResponse.Json json = new ImmutableIdCommandResponse.Json();

    // Act and Assert
    assertSame(json.id, ImmutableIdCommandResponse.fromJson(json).id());
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("IdCommandResponse{}",
        ImmutableIdCommandResponse.fromJson(new ImmutableIdCommandResponse.Json()).toString());
  }

  @Test
  public void withIdTest() {
    // Arrange, Act and Assert
    assertEquals("IdCommandResponse{id=42}",
        ImmutableIdCommandResponse.fromJson(new ImmutableIdCommandResponse.Json()).withId(42L).toString());
  }
}

