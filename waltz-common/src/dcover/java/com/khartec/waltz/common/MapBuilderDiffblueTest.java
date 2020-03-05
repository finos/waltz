package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.HashMap;
import org.junit.Test;

public class MapBuilderDiffblueTest {
  @Test
  public void addTest() {
    // Arrange
    MapBuilder<Object, Object> mapBuilder = new MapBuilder<Object, Object>();

    // Act and Assert
    assertSame(mapBuilder, mapBuilder.add("k", "k"));
  }

  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new MapBuilder<Object, Object>()).build().size());
  }

  @Test
  public void fromTest() {
    // Arrange
    MapBuilder<Object, Object> mapBuilder = new MapBuilder<Object, Object>();
    HashMap<Object, Object> objectObjectMap = new HashMap<Object, Object>();
    objectObjectMap.put("foo", "foo");

    // Act and Assert
    assertSame(mapBuilder, mapBuilder.from(objectObjectMap));
  }
}

