package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_pushTest {

    @Test
    public void pushSingleElement(){
        List<String> element = ListUtilities.newArrayList("a");
        String[] elementToPush = {"b"};
        List<String> result = ListUtilities.push(element,elementToPush);
        assertLength(result, 2);
        assertEquals("a",result.get(0));
        assertEquals("b",result.get(1));
    }

    @Test
    public void pushEmptyStringElement(){
        List<String> element = ListUtilities.newArrayList("a");
        String[] elementToPush = {""};
        List<String> result = ListUtilities.push(element,elementToPush);
        assertLength(result, 2);
        assertEquals("a",result.get(0));
        assertEquals("",result.get(1));
    }

    @Test
    public void pushNullElement(){
        List<String> element = ListUtilities.newArrayList("a");
        String[] elementToPush = {null};
        List<String> result = ListUtilities.push(element,elementToPush);
        assertLength(result, 2);
        assertEquals("a",result.get(0));
        assertEquals(null,result.get(1));
    }
}
