package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;

public class SetUtilitiesDiffblueTest {
  @Test
  public void asSetTest() {
    // Arrange, Act and Assert
    assertEquals(1, SetUtilities.<Object>asSet("foo", "foo", "foo").size());
  }

  @Test
  public void fromArrayTest() {
    // Arrange, Act and Assert
    assertEquals(1, SetUtilities.<Object>fromArray("foo", "foo", "foo").size());
  }

  @Test
  public void fromCollectionTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals(1, SetUtilities.<Object>fromCollection(objectList).size());
  }

  @Test
  public void intersectionTest() {
    // Arrange
    HashSet<Object> objectSet = new HashSet<Object>();
    objectSet.add("foo");
    HashSet<Object> objectSet1 = new HashSet<Object>();
    objectSet1.add("foo");

    // Act and Assert
    assertEquals(1, SetUtilities.<Object>intersection(objectSet, objectSet1).size());
  }

  @Test
  public void minusTest() {
    // Arrange
    HashSet<Object> objectSet = new HashSet<Object>();
    objectSet.add("foo");
    HashSet<Object> objectSet1 = new HashSet<Object>();
    objectSet1.add("foo");
    HashSet<Object> objectSet2 = new HashSet<Object>();
    objectSet2.add("foo");

    // Act and Assert
    assertEquals(0, SetUtilities.<Object>minus(objectSet, objectSet1, objectSet2, objectSet2).size());
  }

  @Test
  public void orderedUnionTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");
    ArrayList<Object> objectList1 = new ArrayList<Object>();
    objectList1.add("foo");
    ArrayList<Object> objectList2 = new ArrayList<Object>();
    objectList2.add("foo");

    // Act and Assert
    assertEquals(1, SetUtilities.<Object>orderedUnion(objectList, objectList1, objectList2).size());
  }

  @Test
  public void unionAllTest() {
    // Arrange
    ArrayList<Collection<Object>> collectionList = new ArrayList<Collection<Object>>();
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");
    collectionList.add(objectList);

    // Act and Assert
    assertEquals(1, SetUtilities.<Object>unionAll(collectionList).size());
  }

  @Test
  public void unionTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");
    ArrayList<Object> objectList1 = new ArrayList<Object>();
    objectList1.add("foo");
    ArrayList<Object> objectList2 = new ArrayList<Object>();
    objectList2.add("foo");

    // Act and Assert
    assertEquals(1, SetUtilities.<Object>union(objectList, objectList1, objectList2).size());
  }
}

