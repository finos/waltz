package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

public class RandomUtilitiesDiffblueTest {

  @Test
  public void randomPickTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals("foo", RandomUtilities.<Object>randomPick((Collection<Object>) objectList));
  }

  @Test
  public void randomPickTest2() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(1, RandomUtilities.<Object>randomPick(objectList, 1).size());
  }

  @Test
  public void randomPickTest3() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(1, RandomUtilities.<Object>randomPick(objectList, 38).size());
  }

  @Test
  public void randomPickTest4() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(0, RandomUtilities.<Object>randomPick(objectList, 0).size());
  }

  @Test
  public void randomPickTest5() {
    // Arrange, Act and Assert
    assertEquals("foo", RandomUtilities.<Object>randomPick("foo", "foo", "foo"));
  }

  @Test
  public void randomPickTest6() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals("foo", RandomUtilities.<Object>randomPick((List<Object>) objectList));
  }
}

