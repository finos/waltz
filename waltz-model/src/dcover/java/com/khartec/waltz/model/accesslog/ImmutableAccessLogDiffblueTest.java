package com.khartec.waltz.model.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableAccessLogDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAccessLog.Json actualJson = new ImmutableAccessLog.Json();

    // Assert
    assertNull(actualJson.state);
    assertNull(actualJson.createdAt);
    assertNull(actualJson.params);
    assertNull(actualJson.userId);
  }
  @Test
  public void setParamsTest() {
    // Arrange
    ImmutableAccessLog.Json json = new ImmutableAccessLog.Json();

    // Act
    json.setParams("params");

    // Assert
    assertEquals("params", json.params);
  }
  @Test
  public void setStateTest() {
    // Arrange
    ImmutableAccessLog.Json json = new ImmutableAccessLog.Json();

    // Act
    json.setState("state");

    // Assert
    assertEquals("state", json.state);
  }
  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutableAccessLog.Json json = new ImmutableAccessLog.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

