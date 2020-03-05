package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Arrays;
import org.junit.Test;

public class ArrayUtilitiesDiffblueTest {
  @Test
  public void initialTest() {
    // Arrange, Act and Assert
    assertEquals(2, ArrayUtilities.<Object>initial(new Object[]{"foo", "foo", "foo"}).length);
  }

  @Test
  public void isEmptyTest() {
    // Arrange, Act and Assert
    assertFalse(ArrayUtilities.<Object>isEmpty(new Object[]{"foo", "foo", "foo"}));
  }

  @Test
  public void lastTest() {
    // Arrange, Act and Assert
    assertEquals("foo", ArrayUtilities.<Object>last(new Object[]{"foo", "foo", "foo"}));
  }

  @Test
  public void sumTest() {
    // Arrange
    int[] intArray = new int[8];
    Arrays.fill(intArray, 1);

    // Act and Assert
    assertEquals(8, ArrayUtilities.sum(intArray));
  }
}

