package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import org.junit.Test;

public class ListUtilitiesDiffblueTest {
  @Test
  public void appendTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(2, ListUtilities.<Object>append(objectList, "foo").size());
  }

  @Test
  public void asListTest() {
    // Arrange, Act and Assert
    assertEquals(3, ListUtilities.<Object>asList("foo", "foo", "foo").size());
  }

  @Test
  public void compactTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(1, ListUtilities.<Object>compact(objectList).size());
  }

  @Test
  public void dropTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(0, ListUtilities.<Object>drop(objectList, 3).size());
  }

  @Test
  public void ensureNotNullTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(1, ListUtilities.<Object>ensureNotNull(objectList).size());
  }

  @Test
  public void isEmptyTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertFalse(ListUtilities.<Object>isEmpty(objectList));
  }

  @Test
  public void newArrayListTest() {
    // Arrange, Act and Assert
    assertEquals(3, ListUtilities.<Object>newArrayList("foo", "foo", "foo").size());
  }

  @Test
  public void pushTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(4, ListUtilities.<Object>push(objectList, "foo", "foo", "foo").size());
  }

  @Test
  public void reverseTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(1, ListUtilities.<Object>reverse(objectList).size());
  }
}

