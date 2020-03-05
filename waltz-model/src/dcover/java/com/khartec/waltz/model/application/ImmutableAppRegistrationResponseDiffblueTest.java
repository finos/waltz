package com.khartec.waltz.model.application;

import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableAppRegistrationResponseDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAppRegistrationResponse.Json actualJson = new ImmutableAppRegistrationResponse.Json();

    // Assert
    assertSame(actualJson.message, actualJson.id);
  }
  @Test
  public void setOriginalRequestTest() {
    // Arrange
    ImmutableAppRegistrationResponse.Json json = new ImmutableAppRegistrationResponse.Json();
    ImmutableAppRegistrationRequest.Json json1 = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setOriginalRequest(json1);

    // Assert
    assertSame(json1, json.originalRequest);
  }
}

