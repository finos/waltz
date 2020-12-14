package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_map {

    @Test
    public void mapSingleElement(){
        Collection<Integer> element = ListUtilities.newArrayList(1);
        List<Integer> result = ListUtilities.map(element, e -> e);
        assertEquals(result.size(), 1);
        assertEquals(1, result.get(0).intValue());

    }

    @Test
    public void mapEmptySingleElement(){
        Collection<String> element = ListUtilities.newArrayList("");
        List<String> result = ListUtilities.map(element, e -> e);
        assertEquals(result.size(), 1);
        assertEquals("", result.get(0));

    }
}
