package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_lengthTest {

    @Test
    public void simpleLength(){
        assertEquals(1, StringUtilities.length("a"));
    }

    @Test
    public void simpleLengthWithEmptyStr(){
        assertEquals(0, StringUtilities.length(""));
    }

    @Test
    public void simpleLengthWithWhitespaceStr(){
        assertEquals(4, StringUtilities.length("    "));
    }

    @Test
    public void simpleLengthWithNullStr(){
        assertEquals(0, StringUtilities.length(null));
    }
}
