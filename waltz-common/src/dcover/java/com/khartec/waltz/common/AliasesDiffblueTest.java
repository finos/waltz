package com.khartec.waltz.common;

import static org.junit.Assert.assertSame;
import org.junit.Test;

public class AliasesDiffblueTest {
  @Test
  public void registerTest() {
    // Arrange
    Aliases<Object> aliases = new Aliases<Object>();

    // Act and Assert
    assertSame(aliases, aliases.register("val", "foo", "foo", "foo"));
  }
}

