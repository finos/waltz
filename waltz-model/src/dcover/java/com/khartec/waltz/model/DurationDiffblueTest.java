package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DurationDiffblueTest {
  @Test
  public void numDaysTest() {
    // Arrange, Act and Assert
    assertEquals(1, Duration.DAY.numDays());
  }
}

