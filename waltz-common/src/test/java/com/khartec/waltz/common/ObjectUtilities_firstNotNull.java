package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectUtilities_firstNotNull {

    @Test
    public void simpleFirstNotNull() {
     String[] ele = {"a", "b"};
     String result = ObjectUtilities.firstNotNull(ele);
     assertEquals("a", result);
    }

    @Test
    public void firstNotNullWithFirstNull() {
        String[] ele = {null, "b"};
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals("b", result);
    }

    @Test
    public void firstNotNullWithFirstEmpty() {
        String[] ele = {"", "b"};
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals("", result);
    }

    @Test
    public void firstNotNullWithEmptyArray() {
        String[] ele = { };
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals(null, result);
    }

    @Test(expected = NullPointerException.class)
    public void firstNotNullWithNullArray() {
        String[] ele = null;
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals(null, result);
    }
}
