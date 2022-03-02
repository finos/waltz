package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CollectionUtilities_sort_one_paramTest {
    @Test
    public void sortNullCollection() {
        assertThrows(IllegalArgumentException.class,
                () -> CollectionUtilities.sort(null));
    }

    @Test
    public void sortEmptyElements(){
        List<String> elements = new ArrayList<>();
        elements.add("");
        elements.add("");
        List<String> result = CollectionUtilities.sort(elements);
        assertEquals(elements.size(),result.size());
        assertEquals("",result.get(0));
        assertEquals("",result.get(1));
    }

    @Test
    public void sortElements(){
        List<String> elements = new ArrayList<>();
        elements.add("x");
        elements.add("a");
        List<String> result = CollectionUtilities.sort(elements);
        assertEquals(elements.size(),result.size());
        assertEquals("a",result.get(0));
        assertEquals("x",result.get(1));
    }
}
