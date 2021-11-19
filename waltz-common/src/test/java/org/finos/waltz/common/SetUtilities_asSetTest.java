package org.finos.waltz.common;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SetUtilities_asSetTest {

    @Test
    public void simpleAsSet(){
        String[] arr = {"a","a","b"};
        Set result = SetUtilities.asSet(arr);
        assertEquals(2,result.size());
        assertEquals("a",result.toArray()[0]);
        assertEquals("b",result.toArray()[1]);
    }

    @Test
    public void asSetWithEmptyArr(){
        String[] arr = {};
        Set result = SetUtilities.asSet(arr);
        assertEquals(0,result.size());
    }

    @Test
    public void fasSetWithNullArr(){
        String[] arr = null;
        Set result = SetUtilities.asSet(arr);
        assertEquals(0,result.size());
    }
}
