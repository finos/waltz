package com.khartec.waltz.common;
import org.junit.Test;

import java.util.List;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_asList {

    @Test
    public void oneElementAsList(){
        String element = "a";
        List<String> result = ListUtilities.asList(element);
        assertLength(result, 1);
        assertEquals("a", result.get(0));
    }

    @Test
    public void nullElementAsList(){
        String element = null;
        List<String> result = ListUtilities.asList(element);
        assertLength(result, 1);
        assertEquals(null, result.get(0));
    }

    @Test
    public void emptyElementAsList(){
        String element = "";
        List<String> result = ListUtilities.asList(element);
        assertLength(result, 1);
        assertEquals("", result.get(0));
    }
}
