package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableUserRegistrationRequestDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUserRegistrationRequest.Json actualJson = new ImmutableUserRegistrationRequest.Json();

    // Assert
    assertNull(actualJson.password);
    assertNull(actualJson.userName);
  }
  @Test
  public void setPasswordTest() {
    // Arrange
    ImmutableUserRegistrationRequest.Json json = new ImmutableUserRegistrationRequest.Json();

    // Act
    json.setPassword("Password123");

    // Assert
    assertEquals("Password123", json.password);
  }
  @Test
  public void setUserNameTest() {
    // Arrange
    ImmutableUserRegistrationRequest.Json json = new ImmutableUserRegistrationRequest.Json();

    // Act
    json.setUserName("username");

    // Assert
    assertEquals("username", json.userName);
  }
}

