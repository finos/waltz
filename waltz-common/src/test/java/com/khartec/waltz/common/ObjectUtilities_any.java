package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectUtilities_any {

    @Test
    public void simpleAny() {
        String[] ele = {"a", "bb"};
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertEquals(true, result);
    }

    @Test
    public void simpleAnyNoMatch() {
        String[] ele = {"aa", "bb"};
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertEquals(false, result);
    }

    @Test
    public void simpleAnyEmptyArray() {
        String[] ele = {};
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertEquals(false, result);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyNullArray() {
        String[] ele = null;
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertEquals(false, result);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyNullPredicate() {
        String[] ele = {"aa", "bb"};
        boolean result = ObjectUtilities.any(null, ele);
        assertEquals(false, result);
    }

    @Test(expected = NullPointerException.class)
    public void simpleAnyAllNull() {
        String[] ele = null;
        boolean result = ObjectUtilities.any(null, ele);
        assertEquals(false, result);
    }
}
