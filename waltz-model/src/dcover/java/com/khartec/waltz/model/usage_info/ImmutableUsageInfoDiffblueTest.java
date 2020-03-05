package com.khartec.waltz.model.usage_info;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableUsageInfoDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUsageInfo.Json actualJson = new ImmutableUsageInfo.Json();

    // Assert
    assertNull(actualJson.kind);
    assertFalse(actualJson.isSelected);
    assertNull(actualJson.description);
    assertFalse(actualJson.isSelectedIsSet);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableUsageInfo.Json json = new ImmutableUsageInfo.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setIsSelectedTest() {
    // Arrange
    ImmutableUsageInfo.Json json = new ImmutableUsageInfo.Json();

    // Act
    json.setIsSelected(true);

    // Assert
    assertTrue(json.isSelected);
    assertTrue(json.isSelectedIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableUsageInfo.Json json = new ImmutableUsageInfo.Json();

    // Act
    json.setKind(UsageKind.CONSUMER);

    // Assert
    assertEquals(UsageKind.CONSUMER, json.kind);
  }
}

