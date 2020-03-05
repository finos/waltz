package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RangeBandDiffblueTest {

  @Test
  public void constructorTest2() {
    // Arrange and Act
    RangeBand<Float> actualRangeBand = new RangeBand<Float>(10.0f, 10.0f);

    // Assert
    assertEquals(Float.valueOf(10.0f), actualRangeBand.getHigh());
    assertEquals(Float.valueOf(10.0f), actualRangeBand.getLow());
  }

  @Test
  public void containsTest() {
    // Arrange, Act and Assert
    assertTrue((new RangeBand<Float>(10.0f, 10.0f)).contains(10.0f));
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse((new RangeBand<Float>(10.0f, 10.0f)).equals("o"));
  }

  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(603979776, (new RangeBand<Float>(10.0f, 10.0f)).hashCode());
  }

  @Test
  public void testTest() {
    // Arrange, Act and Assert
    assertFalse((new RangeBand<Float>(10.0f, 10.0f)).test(0.5f));
  }

  @Test
  public void testTest2() {
    // Arrange, Act and Assert
    assertTrue((new RangeBand<Float>(10.0f, 10.0f)).test(10.0f));
  }

  @Test
  public void testTest3() {
    // Arrange, Act and Assert
    assertFalse((new RangeBand<Float>(10.0f, 10.0f)).test(Float.NaN));
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("10.0 - 10.0", (new RangeBand<Float>(10.0f, 10.0f)).toString());
  }
}

