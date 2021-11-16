package org.finos.waltz.common.hierarchy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Node_addChildTest {

    @Test
    public void simpleAddChild(){
        Node n = new Node("a",1);
        n.setParent(n);
        n.addChild(new Node("b",2));
        assertEquals("a", n.getParent().getId());
        System.out.println(n.getChildren());
    }
}
