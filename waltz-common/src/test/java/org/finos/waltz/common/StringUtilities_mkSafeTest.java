package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_mkSafeTest {

    @Test
    public void simpleMkSafe(){
        assertEquals("a", StringUtilities.mkSafe("a"));
    }

    @Test
    public void simpleMkSafeWithEmptyStr(){
        assertEquals("", StringUtilities.mkSafe(""));
    }

    @Test
    public void simpleMkSafeWithNullStr(){
        assertEquals("", StringUtilities.mkSafe(null));
    }
}
