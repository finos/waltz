package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_appendTest {

    @Test
    public void appendSingleElement(){
        List<String> arr = ListUtilities.newArrayList("a");
        String element = "b";
        List<String> result = ListUtilities.append(arr,element);
        assertLength(result, 2);
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test
    public void appendToEmptyList(){
        List<String> arr = ListUtilities.newArrayList();
        String element = "b";
        List<String> result = ListUtilities.append(arr,element);
        assertLength(result, 1);
        assertEquals("b", result.get(0));
    }

    @Test
    public void appendNullToList(){
        List<String> arr = ListUtilities.newArrayList("a");
        List<String> result = ListUtilities.append(arr,null);
        assertLength(result, 2);
        assertEquals("a", result.get(0));
        assertEquals(null, result.get(1));
    }

    @Test
    public void appendEmptyStringToList(){
        List<String> arr = ListUtilities.newArrayList("a");
        String element = "";
        List<String> result = ListUtilities.append(arr,element);
        assertLength(result, 2);
        assertEquals("a", result.get(0));
        assertEquals("", result.get(1));
    }

    @Test
    public void appendNullToEmptyList(){
        List<String> arr = ListUtilities.newArrayList();
        List<String> result = ListUtilities.append(arr,null);
        assertLength(result, 1);
        assertEquals(null, result.get(0));
    }

    @Test
    public void appendEmptyStringToEmptyList(){
        List<String> arr = ListUtilities.newArrayList();
        String element = "";
        List<String> result = ListUtilities.append(arr,element);
        assertLength(result, 1);
        assertEquals("", result.get(0));
    }
}
