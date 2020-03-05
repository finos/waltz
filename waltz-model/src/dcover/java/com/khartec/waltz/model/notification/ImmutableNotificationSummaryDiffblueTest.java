package com.khartec.waltz.model.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityKind;
import org.junit.Test;

public class ImmutableNotificationSummaryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableNotificationSummary.Json actualJson = new ImmutableNotificationSummary.Json();

    // Assert
    assertNull(actualJson.kind);
    assertEquals(0, actualJson.count);
    assertFalse(actualJson.countIsSet);
  }
  @Test
  public void setCountTest() {
    // Arrange
    ImmutableNotificationSummary.Json json = new ImmutableNotificationSummary.Json();

    // Act
    json.setCount(3);

    // Assert
    assertEquals(3, json.count);
    assertTrue(json.countIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableNotificationSummary.Json json = new ImmutableNotificationSummary.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
}

