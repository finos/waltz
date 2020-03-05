package com.khartec.waltz.model.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class NotAuthorizedExceptionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    NotAuthorizedException actualNotAuthorizedException = new NotAuthorizedException();

    // Assert
    assertEquals("com.khartec.waltz.model.exceptions.NotAuthorize" + "dException: User is not authorized to perform"
        + " that action", actualNotAuthorizedException.toString());
    assertEquals("User is not authorized to perform that action", actualNotAuthorizedException.getLocalizedMessage());
    assertNull(actualNotAuthorizedException.getCause());
    assertEquals("User is not authorized to perform that action", actualNotAuthorizedException.getMessage());
    assertEquals(0, actualNotAuthorizedException.getSuppressed().length);
  }

  @Test
  public void constructorTest2() {
    // Arrange and Act
    NotAuthorizedException actualNotAuthorizedException = new NotAuthorizedException("An error occurred");

    // Assert
    assertEquals("com.khartec.waltz.model.exceptions.NotAuthorizedException:" + " An error occurred",
        actualNotAuthorizedException.toString());
    assertEquals("An error occurred", actualNotAuthorizedException.getLocalizedMessage());
    assertNull(actualNotAuthorizedException.getCause());
    assertEquals("An error occurred", actualNotAuthorizedException.getMessage());
    assertEquals(0, actualNotAuthorizedException.getSuppressed().length);
  }
}

