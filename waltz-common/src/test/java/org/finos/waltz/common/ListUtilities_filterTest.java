package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.*;

public class ListUtilities_filterTest {

    @Test
    public void elementFound(){
        List<String> elements = ListUtilities.newArrayList("a","b","c");
        List<String> result = ListUtilities.filter(e -> e.equals("a"), elements);
        assertFalse(result.isEmpty());
        assertEquals("a",result.get(0));
    }

    @Test
    public void elementNotFound(){
        List<String> elements = ListUtilities.newArrayList("a","b","c");
        List<String> result = ListUtilities.filter(e -> e.equals("z"), elements);
        assertTrue(result.isEmpty());
    }

    @Test
    public void emptyElementFound(){
        List<String> elements = ListUtilities.newArrayList("a","b","");
        List<String> result = ListUtilities.filter(e -> e.equals(""), elements);
        assertFalse(result.isEmpty());
        assertEquals("",result.get(0));
    }

    @Test
    public void emptyElementNotFound(){
        List<String> elements = ListUtilities.newArrayList("a","b","c");
        List<String> result = ListUtilities.filter(e -> e.equals(""), elements);
        assertTrue(result.isEmpty());
    }
}
