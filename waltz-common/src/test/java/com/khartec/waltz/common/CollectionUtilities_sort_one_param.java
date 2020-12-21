package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.khartec.waltz.common.CollectionUtilities.sort;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_sort_one_param {
    @Test(expected = IllegalArgumentException.class)
    public void sortNullCollection(){
        sort(null);
    }

    @Test
    public void sortEmptyElements(){
        List<String> elements = new ArrayList();
        elements.add("");
        elements.add("");
        List<String> result = sort(elements);
        assertEquals(elements.size(),result.size());
        assertEquals("",result.get(0));
        assertEquals("",result.get(1));
    }

    @Test
    public void sortElements(){
        List<String> elements = new ArrayList();
        elements.add("x");
        elements.add("a");
        List<String> result = sort(elements);
        assertEquals(elements.size(),result.size());
        assertEquals("a",result.get(0));
        assertEquals("x",result.get(1));
    }
}
