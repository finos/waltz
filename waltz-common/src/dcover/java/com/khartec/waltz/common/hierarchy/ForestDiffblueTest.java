package com.khartec.waltz.common.hierarchy;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.Test;

public class ForestDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange
    Node<Object, Object> param1 = new Node<Object, Object>("123", "foo");
    HashMap<Object, Node<Object, Object>> objectNodeMap = new HashMap<Object, Node<Object, Object>>();
    objectNodeMap.put("foo", param1);
    HashSet<Node<Object, Object>> nodeSet = new HashSet<Node<Object, Object>>();
    nodeSet.add(new Node<Object, Object>("foo", "123"));

    // Act and Assert
    assertEquals("Forest{#allNodes=1, rootNodes=[Node{id=foo," + " #children=0}]}",
        (new Forest<Object, Object>(objectNodeMap, nodeSet)).toString());
  }

  @Test
  public void toStringTest() {
    // Arrange
    Node<Object, Object> param1 = new Node<Object, Object>("123", "foo");
    HashMap<Object, Node<Object, Object>> objectNodeMap = new HashMap<Object, Node<Object, Object>>();
    objectNodeMap.put("foo", param1);
    HashSet<Node<Object, Object>> nodeSet = new HashSet<Node<Object, Object>>();
    nodeSet.add(new Node<Object, Object>("foo", "123"));

    // Act and Assert
    assertEquals("Forest{#allNodes=1, rootNodes=[Node{id=foo," + " #children=0}]}",
        (new Forest<Object, Object>(objectNodeMap, nodeSet)).toString());
  }
}

