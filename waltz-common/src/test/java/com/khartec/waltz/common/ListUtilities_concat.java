package com.khartec.waltz.common;

import org.junit.Test;

import java.util.List;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_concat {

    @Test
    public void concatSingleListElement(){
        List<String>  element = ListUtilities.newArrayList("a");
        List<String> result = ListUtilities.concat(element);
        assertLength(result, 1);
        assertEquals("a", result.get(0));
    }

    @Test
    public void concatEmptyListElement(){
        List<String>  element = ListUtilities.newArrayList("");
        List<String> result = ListUtilities.concat(element);
        assertLength(result, 1);
        assertEquals("", result.get(0));
    }
}
