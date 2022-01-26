package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListUtilities_dropTest {

    @Test
    public void dropOneElement(){
        List<String> elements = ListUtilities.newArrayList("a","b","c");
        List<String> result = ListUtilities.drop(elements,1);
        assertLength(result, 2);
        assertEquals("b",result.get(0));
        assertEquals("c",result.get(1));
    }

    @Test
    public void dropAllElements(){
        List<String> elements = ListUtilities.newArrayList("a","b","c");
        List<String> result = ListUtilities.drop(elements,3);
        assertLength(result, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    public void dropNoElements(){
        List<String> elements = ListUtilities.newArrayList("a","b","c");
        List<String> result = ListUtilities.drop(elements,0);
        assertLength(result, 3);
        assertEquals("a",result.get(0));
        assertEquals("b",result.get(1));
        assertEquals("c",result.get(2));
    }
}
