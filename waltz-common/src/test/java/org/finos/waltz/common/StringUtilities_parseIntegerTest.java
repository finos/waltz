package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_parseIntegerTest {

    @Test
    public void simpleParseInteger(){
        assertEquals((Integer)1, StringUtilities.parseInteger("1", 0));
    }

    @Test
    public void simpleParseIntegerWithAlpha(){
        assertEquals((Integer)0, StringUtilities.parseInteger("a", 0));
    }

    @Test
    public void simpleParseIntegerWithEmptyStr(){
        assertEquals((Integer)0, StringUtilities.parseInteger("", 0));
    }

    @Test
    public void simpleParseIntegerWithNullStr(){
        assertEquals((Integer)0, StringUtilities.parseInteger(null, 0));
    }

    @Test
    public void simpleParseIntegerWithBothNull(){
        assertEquals(null, StringUtilities.parseInteger(null, null));
    }
}
