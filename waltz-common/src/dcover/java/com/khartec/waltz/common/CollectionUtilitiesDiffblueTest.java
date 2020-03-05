package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CollectionUtilitiesDiffblueTest {
  @Test
  public void firstTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals("foo", CollectionUtilities.<Object>first(objectList));
  }

  @Test
  public void isEmptyTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertFalse(CollectionUtilities.<Object>isEmpty(objectList));
  }

  @Test
  public void notEmptyTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertTrue(CollectionUtilities.<Object>notEmpty(objectList));
  }

  @Test
  public void sortTest() {
    // Arrange
    ArrayList<Float> resultFloatList = new ArrayList<Float>();
    resultFloatList.add(10.0f);

    // Act
    List<Float> actualSortResult = CollectionUtilities.<Float>sort(resultFloatList);

    // Assert
    assertEquals(1, actualSortResult.size());
    assertEquals(Float.valueOf(10.0f), actualSortResult.get(0));
  }

  @Test
  public void sumIntsTest() {
    // Arrange
    ArrayList<Integer> integerList = new ArrayList<Integer>();
    integerList.add(1);

    // Act and Assert
    assertEquals(Long.valueOf(1L), CollectionUtilities.sumInts(integerList));
  }

  @Test
  public void sumLongsTest() {
    // Arrange
    ArrayList<Long> resultLongList = new ArrayList<Long>();
    resultLongList.add(1L);

    // Act and Assert
    assertEquals(Long.valueOf(1L), CollectionUtilities.sumLongs(resultLongList));
  }
}

