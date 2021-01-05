package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilities_length {

    @Test
    public void simpleLength(){
        assertEquals(1, StringUtilities.length("a"));
    }

    @Test
    public void simpleLengthWithEmptyStr(){
        assertEquals(0, StringUtilities.length(""));
    }

    @Test
    public void simpleLengthWithNullStr(){
        assertEquals(0, StringUtilities.length(null));
    }
}
