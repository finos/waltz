package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.*;

public class ListUtilities_applyToFirst {

    @Test
    public void applyOnEmptyList(){
        List<Integer> element = new ArrayList();
        Optional<Integer> result = ListUtilities.applyToFirst(element, e-> e*2);
        assertFalse(result.isPresent());
    }

    @Test
    public void applyOnSingleElementList(){
        List<Integer> element = new ArrayList(1);
        Optional<Integer> result = ListUtilities.applyToFirst(element, e-> e*2);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().intValue());
    }
}
