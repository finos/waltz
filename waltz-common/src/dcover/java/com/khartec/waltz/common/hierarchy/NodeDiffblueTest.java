package com.khartec.waltz.common.hierarchy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class NodeDiffblueTest {
  @Test
  public void addChildTest() {
    // Arrange
    Node<Object, Object> node = new Node<Object, Object>("123", "123");

    // Act and Assert
    assertSame(node, node.addChild(new Node("123", "123")));
  }

  @Test
  public void constructorTest() {
    // Arrange and Act
    Node<Object, Object> actualNode = new Node<Object, Object>("123", "123");

    // Assert
    assertEquals("Node{id=123, #children=0}", actualNode.toString());
    assertNull(actualNode.getParent());
    assertTrue(actualNode.getData() instanceof String);
  }

  @Test
  public void getChildrenTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new Node<Object, Object>("123", "123")).getChildren().size());
  }

  @Test
  public void setParentTest() {
    // Arrange
    Node<Object, Object> node = new Node<Object, Object>("123", "123");
    Node<Object, Object> node1 = new Node<Object, Object>("123", "123");

    // Act
    Node actualSetParentResult = node.setParent(node1);

    // Assert
    assertSame(node, actualSetParentResult);
    assertSame(node1, actualSetParentResult.getParent());
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("Node{id=123, #children=0}", (new Node<Object, Object>("123", "123")).toString());
  }
}

