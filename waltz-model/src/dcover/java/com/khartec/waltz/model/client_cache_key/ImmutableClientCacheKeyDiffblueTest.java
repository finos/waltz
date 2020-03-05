package com.khartec.waltz.model.client_cache_key;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableClientCacheKeyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableClientCacheKey.Json actualJson = new ImmutableClientCacheKey.Json();

    // Assert
    assertNull(actualJson.key);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.guid);
  }
  @Test
  public void setGuidTest() {
    // Arrange
    ImmutableClientCacheKey.Json json = new ImmutableClientCacheKey.Json();

    // Act
    json.setGuid("12345678");

    // Assert
    assertEquals("12345678", json.guid);
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableClientCacheKey.Json json = new ImmutableClientCacheKey.Json();

    // Act
    json.setKey("key");

    // Assert
    assertEquals("key", json.key);
  }
}

