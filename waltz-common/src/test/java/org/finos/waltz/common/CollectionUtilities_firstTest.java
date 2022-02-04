package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionUtilities_firstTest {

    @Test
    public void sendNull() {
        assertThrows(IllegalArgumentException.class,
                () -> CollectionUtilities.first(null));
    }

    @Test
    public void sendElements(){
        Collection element = new ArrayList();
        element.add("a");
        element.add("b");
        Object result = CollectionUtilities.first(element);
        assertEquals("a", result);
    }
}
