package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutablePasswordResetRequestDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePasswordResetRequest.Json actualJson = new ImmutablePasswordResetRequest.Json();

    // Assert
    assertNull(actualJson.newPassword);
    assertNull(actualJson.userName);
    assertNull(actualJson.currentPassword);
  }
  @Test
  public void setCurrentPasswordTest() {
    // Arrange
    ImmutablePasswordResetRequest.Json json = new ImmutablePasswordResetRequest.Json();

    // Act
    json.setCurrentPassword("Password123");

    // Assert
    assertEquals("Password123", json.currentPassword);
  }
  @Test
  public void setNewPasswordTest() {
    // Arrange
    ImmutablePasswordResetRequest.Json json = new ImmutablePasswordResetRequest.Json();

    // Act
    json.setNewPassword("Password123");

    // Assert
    assertEquals("Password123", json.newPassword);
  }
  @Test
  public void setUserNameTest() {
    // Arrange
    ImmutablePasswordResetRequest.Json json = new ImmutablePasswordResetRequest.Json();

    // Act
    json.setUserName("username");

    // Assert
    assertEquals("username", json.userName);
  }
}

