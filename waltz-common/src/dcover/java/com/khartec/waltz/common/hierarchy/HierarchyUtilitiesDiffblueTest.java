package com.khartec.waltz.common.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Test;

public class HierarchyUtilitiesDiffblueTest {
  @Test
  public void assignDepthsTest() {
    // Arrange
    Node<Object, Object> param1 = new Node<Object, Object>("123", "foo");
    HashMap<Object, Node<Object, Object>> objectNodeMap = new HashMap<Object, Node<Object, Object>>();
    objectNodeMap.put("foo", param1);
    HashSet<Node<Object, Object>> nodeSet = new HashSet<Node<Object, Object>>();
    nodeSet.add(new Node<Object, Object>("foo", "123"));

    // Act and Assert
    assertEquals(1,
        HierarchyUtilities.<Object, Object>assignDepths(new Forest<Object, Object>(objectNodeMap, nodeSet)).size());
  }

  @Test
  public void hasCycleTest() {
    // Arrange
    Node<Object, Object> param1 = new Node<Object, Object>("123", "foo");
    HashMap<Object, Node<Object, Object>> objectNodeMap = new HashMap<Object, Node<Object, Object>>();
    objectNodeMap.put("foo", param1);
    HashSet<Node<Object, Object>> nodeSet = new HashSet<Node<Object, Object>>();
    nodeSet.add(new Node<Object, Object>("foo", "123"));

    // Act and Assert
    assertFalse(HierarchyUtilities.<Object, Object>hasCycle(new Forest<Object, Object>(objectNodeMap, nodeSet)));
  }

  @Test
  public void parentsTest() {
    // Arrange, Act and Assert
    assertEquals(0, HierarchyUtilities.<Object, Object>parents(new Node<Object, Object>("123", "123")).size());
  }
}

