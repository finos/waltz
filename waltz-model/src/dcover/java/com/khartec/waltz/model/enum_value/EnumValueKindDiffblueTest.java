package com.khartec.waltz.model.enum_value;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class EnumValueKindDiffblueTest {
  @Test
  public void dbValueTest() {
    // Arrange, Act and Assert
    assertEquals("TransportKind", EnumValueKind.TRANSPORT_KIND.dbValue());
  }
}

