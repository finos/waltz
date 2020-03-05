package com.khartec.waltz.common.exception;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NotFoundExceptionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals("code", (new NotFoundException("code", "An error occurred")).getCode());
  }

  @Test
  public void constructorTest2() {
    // Arrange, Act and Assert
    assertEquals("code", (new NotFoundException("code", "An error occurred", new Throwable())).getCode());
  }
}

