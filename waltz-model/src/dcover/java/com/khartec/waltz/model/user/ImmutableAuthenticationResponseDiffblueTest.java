package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableAuthenticationResponseDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAuthenticationResponse.Json actualJson = new ImmutableAuthenticationResponse.Json();

    // Assert
    assertNull(actualJson.waltzUserName);
    assertFalse(actualJson.successIsSet);
    assertNull(actualJson.errorMessage);
    assertFalse(actualJson.success);
  }


  @Test
  public void setErrorMessageTest() {
    // Arrange
    ImmutableAuthenticationResponse.Json json = new ImmutableAuthenticationResponse.Json();

    // Act
    json.setErrorMessage("An error occurred");

    // Assert
    assertEquals("An error occurred", json.errorMessage);
  }

  @Test
  public void setSuccessTest() {
    // Arrange
    ImmutableAuthenticationResponse.Json json = new ImmutableAuthenticationResponse.Json();

    // Act
    json.setSuccess(true);

    // Assert
    assertTrue(json.successIsSet);
    assertTrue(json.success);
  }

  @Test
  public void setWaltzUserNameTest() {
    // Arrange
    ImmutableAuthenticationResponse.Json json = new ImmutableAuthenticationResponse.Json();

    // Act
    json.setWaltzUserName("username");

    // Assert
    assertEquals("username", json.waltzUserName);
  }
}

