package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableReleaseLifecycleStatusChangeCommandDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableReleaseLifecycleStatusChangeCommand.Json()).newStatus);
  }

  @Test
  public void setNewStatusTest() {
    // Arrange
    ImmutableReleaseLifecycleStatusChangeCommand.Json json = new ImmutableReleaseLifecycleStatusChangeCommand.Json();

    // Act
    json.setNewStatus(ReleaseLifecycleStatus.DRAFT);

    // Assert
    assertEquals(ReleaseLifecycleStatus.DRAFT, json.newStatus);
  }
}

