package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ChecksDiffblueTest {
  @Test
  public void checkNotEmptyTest() {
    // Arrange, Act and Assert
    assertEquals("str", Checks.checkNotEmpty("str", "message"));
  }
  @Test
  public void checkNotNullTest() {
    // Arrange, Act and Assert
    assertEquals("", Checks.<Object>checkNotNull("", "message", "foo", "", "foo"));
  }
}

