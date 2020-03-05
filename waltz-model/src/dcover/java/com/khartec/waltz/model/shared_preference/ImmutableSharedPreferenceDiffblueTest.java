package com.khartec.waltz.model.shared_preference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.junit.Test;

public class ImmutableSharedPreferenceDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSharedPreference.Json actualJson = new ImmutableSharedPreference.Json();

    // Assert
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.category);
    assertNull(actualJson.value);
    assertNull(actualJson.key);
    assertNull(actualJson.lastUpdatedAt);
  }
  @Test
  public void setCategoryTest() {
    // Arrange
    ImmutableSharedPreference.Json json = new ImmutableSharedPreference.Json();

    // Act
    json.setCategory("category");

    // Assert
    assertEquals("category", json.category);
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableSharedPreference.Json json = new ImmutableSharedPreference.Json();

    // Act
    json.setKey("key");

    // Assert
    assertEquals("key", json.key);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableSharedPreference.Json json = new ImmutableSharedPreference.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setValueTest() {
    // Arrange
    ImmutableSharedPreference.Json json = new ImmutableSharedPreference.Json();

    // Act
    json.setValue("value");

    // Assert
    assertEquals("value", json.value);
  }
}

