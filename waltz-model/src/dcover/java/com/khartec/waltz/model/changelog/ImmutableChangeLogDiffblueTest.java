package com.khartec.waltz.model.changelog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import org.junit.Test;

public class ImmutableChangeLogDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableChangeLog.Json actualJson = new ImmutableChangeLog.Json();

    // Assert
    assertNull(actualJson.parentReference);
    assertNull(actualJson.message);
    assertNull(actualJson.operation);
    assertNull(actualJson.userId);
    assertNull(actualJson.severity);
    assertNull(actualJson.createdAt);
  }
  @Test
  public void setMessageTest() {
    // Arrange
    ImmutableChangeLog.Json json = new ImmutableChangeLog.Json();

    // Act
    json.setMessage("message");

    // Assert
    assertEquals("message", json.message);
  }
  @Test
  public void setOperationTest() {
    // Arrange
    ImmutableChangeLog.Json json = new ImmutableChangeLog.Json();

    // Act
    json.setOperation(Operation.ADD);

    // Assert
    assertEquals(Operation.ADD, json.operation);
  }
  @Test
  public void setSeverityTest() {
    // Arrange
    ImmutableChangeLog.Json json = new ImmutableChangeLog.Json();

    // Act
    json.setSeverity(Severity.INFORMATION);

    // Assert
    assertEquals(Severity.INFORMATION, json.severity);
  }
  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutableChangeLog.Json json = new ImmutableChangeLog.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

