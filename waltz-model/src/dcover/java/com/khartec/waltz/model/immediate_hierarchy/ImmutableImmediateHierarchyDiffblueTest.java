package com.khartec.waltz.model.immediate_hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableImmediateHierarchyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableImmediateHierarchy.Json<Object>()).children.size());
  }
  @Test
  public void setChildrenTest() {
    // Arrange
    ImmutableImmediateHierarchy.Json<Object> json = new ImmutableImmediateHierarchy.Json<Object>();
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act
    json.setChildren(objectList);

    // Assert
    assertSame(objectList, json.children);
  }
  @Test
  public void setSelfTest() {
    // Arrange
    ImmutableImmediateHierarchy.Json<Object> json = new ImmutableImmediateHierarchy.Json<Object>();

    // Act
    json.setSelf("self");

    // Assert
    assertTrue(json.self instanceof String);
  }
}

