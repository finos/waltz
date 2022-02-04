package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class StreamUtilities_ofNullableArrayTest {
    @Test
    public void simpleOfNullableArray1() {
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
