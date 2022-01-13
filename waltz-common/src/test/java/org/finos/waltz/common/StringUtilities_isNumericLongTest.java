package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_isNumericLongTest {

    @Test
    public void simpleIsNumericLong(){
        assertEquals(true, StringUtilities.isNumericLong("1"));
    }

    @Test
    public void simpleIsNumericLongWithAlpha(){
        assertEquals(false, StringUtilities.isNumericLong("a"));
    }

    @Test
    public void simpleIsNumericLongWithEmptyStr(){
        assertEquals(false, StringUtilities.isNumericLong(""));
    }

    @Test
    public void simpleIsNumericLongWithNullStr(){
        assertEquals(false, StringUtilities.isNumericLong(null));
    }
}
