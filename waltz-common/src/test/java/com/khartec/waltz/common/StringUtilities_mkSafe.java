package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilities_mkSafe {

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
