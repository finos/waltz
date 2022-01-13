package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetUtilities_fromArrayTest {

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
