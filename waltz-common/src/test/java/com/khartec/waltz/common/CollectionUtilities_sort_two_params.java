package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static com.khartec.waltz.common.CollectionUtilities.sort;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_sort_two_params {

    @Test(expected = IllegalArgumentException.class)
    public void sortNullCollection(){
        sort(null, Comparator.naturalOrder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortWithNullComparator(){
        List<String> elements = new ArrayList();
        elements.add("x");
        elements.add("a");
        sort(elements, null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void sortNullCollectionWithNullComparator(){
        sort(null, null);
    }

    @Test
    public void sortElements(){
        List<String> elements = new ArrayList();
        elements.add("x");
        elements.add("a");
        List<String> result = sort(elements, Comparator.naturalOrder());
        assertEquals(elements.size(),result.size());
        assertEquals("a",result.get(0));
        assertEquals("x",result.get(1));
    }

    @Test
    public void sortEmptyElements(){
        List<String> elements = new ArrayList();
        elements.add("");
        elements.add("");
        List<String> result = sort(elements, Comparator.naturalOrder());
        assertEquals(elements.size(),result.size());
        assertEquals("",result.get(0));
        assertEquals("",result.get(1));
    }
}
