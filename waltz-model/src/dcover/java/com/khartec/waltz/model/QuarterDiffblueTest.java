package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class QuarterDiffblueTest {
    
  @Test
  public void fromIntTest() {
    // Arrange
    Quarter actualFromIntResult = Quarter.fromInt(1);
    Quarter actualFromIntResult1 = Quarter.fromInt(2);
    Quarter actualFromIntResult2 = Quarter.fromInt(3);

    // Act and Assert
    assertEquals(Quarter.Q1, actualFromIntResult);
    assertEquals(Quarter.Q2, actualFromIntResult1);
    assertEquals(Quarter.Q3, actualFromIntResult2);
    assertEquals(Quarter.Q4, Quarter.fromInt(4));
  }
}

