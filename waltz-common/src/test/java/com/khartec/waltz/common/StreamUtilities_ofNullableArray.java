package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;

public class StreamUtilities_ofNullableArray {
    @Test
    public void simpleOfNullableArray1(){
        String[] elements = {"a", "b"};
        Stream t = StreamUtilities.ofNullableArray(elements);
        assertEquals(2, t.count());
    }

    @Test
    public void simpleOfNullableArray2(){
        String[] elements = null;
        Stream t = StreamUtilities.ofNullableArray(elements);
        assertEquals(0, t.count());
    }
}
