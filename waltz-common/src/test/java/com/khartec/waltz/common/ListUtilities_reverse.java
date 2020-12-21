package com.khartec.waltz.common;

import org.junit.Test;

import java.util.List;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_reverse {

    @Test
    public void reverseSingleElementList(){
        List<String> element = ListUtilities.newArrayList("a");
        List<String> result = ListUtilities.reverse(element);
        assertLength(result, element.size());
        assertEquals(element.get(0),result.get(0));
    }

    @Test
    public void reverseMultipleElementList(){
        List<String> elements = ListUtilities.newArrayList("a", "b", "c");
        List<String> result = ListUtilities.reverse(elements);
        assertLength(result, elements.size());
        assertEquals(elements.get(elements.size()-1),result.get(0));
    }
}
