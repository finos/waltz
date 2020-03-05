package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class EntityLifecycleStatusDiffblueTest {
  @Test
  public void fromIsRemovedFlagTest() {
    // Arrange
    EntityLifecycleStatus actualFromIsRemovedFlagResult = EntityLifecycleStatus.fromIsRemovedFlag(true);

    // Act and Assert
    assertEquals(EntityLifecycleStatus.REMOVED, actualFromIsRemovedFlagResult);
    assertEquals(EntityLifecycleStatus.ACTIVE, EntityLifecycleStatus.fromIsRemovedFlag(false));
  }
}

