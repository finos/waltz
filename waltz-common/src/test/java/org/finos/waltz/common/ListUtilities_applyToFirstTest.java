package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.TestUtilities.assertLength;
import static org.junit.jupiter.api.Assertions.*;

public class ListUtilities_applyToFirstTest {

    @Test
    public void applyOnEmptyList(){
        List<Integer> element = new ArrayList();
        Optional<Integer> result = ListUtilities.applyToFirst(element, e-> e*2);
        assertFalse(result.isPresent());
    }

    @Test
    public void applyOnSingleElementList(){
        List<Integer> element = new ArrayList();
        element.add(1);
        Optional<Integer> result = ListUtilities.applyToFirst(element, e-> e*2);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().intValue());
    }

    @Test
    public void applyOnMultipleElementList(){
        List<Integer> elements = new ArrayList();
        elements.add(1);
        elements.add(2);
        elements.add(3);
        Optional<Integer> result = ListUtilities.applyToFirst(elements, e-> e*2);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().intValue());
    }

    @Test
    public void applyOnNullElementList(){
        List<Integer> elements = new ArrayList();
        elements.add(null);
        elements.add(2);
        elements.add(3);
        Optional<Integer> result = ListUtilities.applyToFirst(elements, e-> e*2);
        assertFalse(result.isPresent());
    }
}
