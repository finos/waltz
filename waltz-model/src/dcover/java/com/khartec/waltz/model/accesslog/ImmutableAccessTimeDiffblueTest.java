package com.khartec.waltz.model.accesslog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableAccessTimeDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAccessTime.Json actualJson = new ImmutableAccessTime.Json();

    // Assert
    assertNull(actualJson.userId);
    assertNull(actualJson.createdAt);
  }


  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutableAccessTime.Json json = new ImmutableAccessTime.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

