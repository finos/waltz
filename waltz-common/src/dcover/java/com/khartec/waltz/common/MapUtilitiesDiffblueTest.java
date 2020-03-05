package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class MapUtilitiesDiffblueTest {
  @Test
  public void composeTest() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<Object, Object>();
    objectObjectMap.put("foo", "foo");
    HashMap<Object, Object> objectObjectMap1 = new HashMap<Object, Object>();
    objectObjectMap1.put("foo", "foo");

    // Act and Assert
    assertEquals(1, MapUtilities.<Object, Object, Object>compose(objectObjectMap, objectObjectMap1).size());
  }

  @Test
  public void ensureNotNullTest() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<Object, Object>();
    objectObjectMap.put("foo", "foo");

    // Act
    Map<Object, Object> actualEnsureNotNullResult = MapUtilities.<Object, Object>ensureNotNull(objectObjectMap);

    // Assert
    assertSame(objectObjectMap, actualEnsureNotNullResult);
    assertEquals(1, actualEnsureNotNullResult.size());
  }

  @Test
  public void isEmptyTest() {
    // Arrange
    HashMap<Object, Object> objectObjectMap = new HashMap<Object, Object>();
    objectObjectMap.put("foo", "foo");

    // Act and Assert
    assertFalse(MapUtilities.<Object, Object>isEmpty(objectObjectMap));
  }

  @Test
  public void newHashMapTest() {
    // Arrange, Act and Assert
    assertEquals(1, MapUtilities.<Object, Object>newHashMap("key", "key").size());
  }

  @Test
  public void newHashMapTest2() {
    // Arrange, Act and Assert
    assertEquals(1,
        MapUtilities.<Object, Object>newHashMap("k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1").size());
  }

  @Test
  public void newHashMapTest3() {
    // Arrange, Act and Assert
    assertEquals(1, MapUtilities
        .<Object, Object>newHashMap("k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1").size());
  }

  @Test
  public void newHashMapTest4() {
    // Arrange, Act and Assert
    assertEquals(0, MapUtilities.<Object, Object>newHashMap().size());
  }

  @Test
  public void newHashMapTest5() {
    // Arrange, Act and Assert
    assertEquals(1, MapUtilities.<Object, Object>newHashMap("k1", "k1", "k1", "k1").size());
  }

  @Test
  public void newHashMapTest6() {
    // Arrange, Act and Assert
    assertEquals(1, MapUtilities.<Object, Object>newHashMap("k1", "k1", "k1", "k1", "k1", "k1", "k1", "k1").size());
  }

  @Test
  public void newHashMapTest7() {
    // Arrange, Act and Assert
    assertEquals(1, MapUtilities.<Object, Object>newHashMap("k1", "k1", "k1", "k1", "k1", "k1").size());
  }
}

