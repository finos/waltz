package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class DebugUtilitiesDiffblueTest {
  @Test
  public void dumpTest() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<Object, Object>();
    objectObjectMap.put("foo", "foo");

    // Act
    Map<Object, Object> actualDumpResult = DebugUtilities.<Object, Object>dump(objectObjectMap);

    // Assert
    assertSame(objectObjectMap, actualDumpResult);
    assertEquals(1, actualDumpResult.size());
  }

  @Test
  public void logValueTest() {
    // Arrange, Act and Assert
    assertEquals("result", DebugUtilities.<Object>logValue("result", "result", "foo", "result"));
  }
}

