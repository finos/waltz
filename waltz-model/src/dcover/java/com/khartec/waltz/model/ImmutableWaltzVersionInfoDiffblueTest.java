package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableWaltzVersionInfoDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableWaltzVersionInfo.Json actualJson = new ImmutableWaltzVersionInfo.Json();

    // Assert
    assertNull(actualJson.pomVersion);
    assertNull(actualJson.revision);
    assertNull(actualJson.timestamp);
  }
  @Test
  public void setPomVersionTest() {
    // Arrange
    ImmutableWaltzVersionInfo.Json json = new ImmutableWaltzVersionInfo.Json();

    // Act
    json.setPomVersion("pomVersion");

    // Assert
    assertEquals("pomVersion", json.pomVersion);
  }
  @Test
  public void setRevisionTest() {
    // Arrange
    ImmutableWaltzVersionInfo.Json json = new ImmutableWaltzVersionInfo.Json();

    // Act
    json.setRevision("revision");

    // Assert
    assertEquals("revision", json.revision);
  }
  @Test
  public void setTimestampTest() {
    // Arrange
    ImmutableWaltzVersionInfo.Json json = new ImmutableWaltzVersionInfo.Json();

    // Act
    json.setTimestamp("timestamp");

    // Assert
    assertEquals("timestamp", json.timestamp);
  }
}

