package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.junit.Test;

public class DigestUtilitiesDiffblueTest {
  @Test
  public void digestTest() throws NoSuchAlgorithmException {
    // Arrange
    byte[] byteArray = new byte[24];
    Arrays.fill(byteArray, (byte) 88);

    // Act and Assert
    assertEquals("ooseQ9DejG1ltYq+oL/LANK66RQ=", DigestUtilities.digest(byteArray));
  }
}

