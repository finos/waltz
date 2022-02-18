package org.finos.waltz.common.hierarchy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Node_addChildTest {

    @Test
    public void simpleAddChild(){
        Node<Long, String> n = mkNode("a", 1L);
        n.setParent(n);
        n.addChild(mkNode("b",2L));
        assertEquals("a", n.getParent().getId());
    }


    private Node<Long, String> mkNode(String id,
                                      Long data) {
        return new Node<>(id, data);
    }
}
