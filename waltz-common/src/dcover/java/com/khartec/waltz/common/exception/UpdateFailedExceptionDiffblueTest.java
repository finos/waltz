package com.khartec.waltz.common.exception;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class UpdateFailedExceptionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals("code", (new UpdateFailedException("code", "An error occurred")).getCode());
  }

  @Test
  public void constructorTest2() {
    // Arrange, Act and Assert
    assertEquals("code", (new UpdateFailedException("code", "An error occurred", new Throwable())).getCode());
  }
}

