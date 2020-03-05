package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableLoginRequestDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLoginRequest.Json actualJson = new ImmutableLoginRequest.Json();

    // Assert
    assertNull(actualJson.password);
    assertNull(actualJson.userName);
  }
  @Test
  public void setPasswordTest() {
    // Arrange
    ImmutableLoginRequest.Json json = new ImmutableLoginRequest.Json();

    // Act
    json.setPassword("Password123");

    // Assert
    assertEquals("Password123", json.password);
  }
  @Test
  public void setUserNameTest() {
    // Arrange
    ImmutableLoginRequest.Json json = new ImmutableLoginRequest.Json();

    // Act
    json.setUserName("username");

    // Assert
    assertEquals("username", json.userName);
  }
}

