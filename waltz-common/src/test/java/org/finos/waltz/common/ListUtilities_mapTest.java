package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListUtilities_mapTest {

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
