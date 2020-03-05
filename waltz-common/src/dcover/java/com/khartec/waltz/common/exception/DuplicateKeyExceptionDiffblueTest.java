package com.khartec.waltz.common.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class DuplicateKeyExceptionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange
    Throwable throwable = new Throwable();

    // Act and Assert
    assertSame((new DuplicateKeyException("An error occurred", throwable)).getCause(), throwable);
  }

  @Test
  public void constructorTest2() {
    // Arrange and Act
    DuplicateKeyException actualDuplicateKeyException = new DuplicateKeyException("An error occurred");

    // Assert
    assertEquals("com.khartec.waltz.common.exception.DuplicateKeyException:" + " An error occurred",
        actualDuplicateKeyException.toString());
    assertEquals("An error occurred", actualDuplicateKeyException.getLocalizedMessage());
    assertNull(actualDuplicateKeyException.getCause());
    assertEquals("An error occurred", actualDuplicateKeyException.getMessage());
    assertEquals(0, actualDuplicateKeyException.getSuppressed().length);
  }
}

