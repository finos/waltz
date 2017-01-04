package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArrayUtilities_isEmpty {

    @Test
    public void nullIsEmpty() {
        assertTrue(ArrayUtilities.isEmpty(null));
    }


    @Test
    public void emptyIsEmpty() {
        assertTrue(ArrayUtilities.isEmpty(new String[] {}));
    }


    @Test
    public void arrayWithElementsIsNotEmpty() {
        assertFalse(ArrayUtilities.isEmpty(new String[] {"A"}));
    }

}
