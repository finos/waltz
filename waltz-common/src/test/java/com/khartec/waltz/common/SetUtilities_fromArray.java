package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SetUtilities_fromArray {

    @Test
    public void simpleFromArray(){
        String[] arr = {"a","a","b"};
        Set result = SetUtilities.fromArray(arr);
        assertEquals(2,result.size());
        assertEquals("a",result.toArray()[0]);
        assertEquals("b",result.toArray()[1]);
    }

    @Test
    public void fromArrayWithEmptyArr(){
        String[] arr = {};
        Set result = SetUtilities.fromArray(arr);
        assertEquals(0,result.size());
    }

    @Test
    public void fromArrayWithNullarr(){
        String[] arr = null;
        Set result = SetUtilities.fromArray(arr);
        assertEquals(0,result.size());
    }
}
