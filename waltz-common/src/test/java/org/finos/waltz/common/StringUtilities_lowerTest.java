package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_lowerTest {

    @Test
    public void simpleLowerWithCaps(){
        String str = "ABC";
        String expected = "abc";
        assertEquals(expected, StringUtilities.lower(str));
    }

    @Test
    public void simpleLowerWithLows(){
        String str = "abc";
        String expected = "abc";
        assertEquals(expected, StringUtilities.lower(str));
    }

    @Test
    public void simpleLowerWithEmptyStr(){
        String str = "";
        String expected = "";
        assertEquals(expected, StringUtilities.lower(str));
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleLowerWithNullStr(){
        String str = null;
        StringUtilities.lower(str);
        /*

        assertThrows(NullPointerException.class,
                ()->  StringUtilities.join(coll,","));
         */
    }
}
