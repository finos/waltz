package org.finos.waltz.common;
import org.junit.Test;

import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_asList {

    @Test
    public void oneElementAsList(){
        String element = "a";
        List<String> result = ListUtilities.asList(element);
        TestUtilities.assertLength(result, 1);
        assertEquals("a", result.get(0));
    }

    @Test
    public void nullElementAsList(){
        String element = null;
        List<String> result = ListUtilities.asList(element);
        TestUtilities.assertLength(result, 1);
        assertEquals(null, result.get(0));
    }

    @Test
    public void emptyElementAsList(){
        String element = "";
        List<String> result = ListUtilities.asList(element);
        TestUtilities.assertLength(result, 1);
        assertEquals("", result.get(0));
    }
}
