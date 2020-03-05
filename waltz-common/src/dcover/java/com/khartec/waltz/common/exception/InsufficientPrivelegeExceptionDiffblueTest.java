package com.khartec.waltz.common.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class InsufficientPrivelegeExceptionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    InsufficientPrivelegeException actualInsufficientPrivelegeException = new InsufficientPrivelegeException(
        "An error occurred");

    // Assert
    assertEquals("com.khartec.waltz.common.exception.InsufficientPri" + "velegeException: An error occurred",
        actualInsufficientPrivelegeException.toString());
    assertEquals("An error occurred", actualInsufficientPrivelegeException.getLocalizedMessage());
    assertNull(actualInsufficientPrivelegeException.getCause());
    assertEquals("An error occurred", actualInsufficientPrivelegeException.getMessage());
    assertEquals(0, actualInsufficientPrivelegeException.getSuppressed().length);
  }
}

