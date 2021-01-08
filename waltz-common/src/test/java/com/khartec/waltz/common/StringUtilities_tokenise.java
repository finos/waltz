package com.khartec.waltz.common;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class StringUtilities_tokenise {

    @Test
    public void simpleTokenise(){
        String str = "abc";
        List expectedList = ListUtilities.newArrayList("abc");
        assertEquals(expectedList, StringUtilities.tokenise(str));
    }

    @Test
    public void simpleTokeniseWithEmptyStr(){
        String str = "";
        List expectedList = ListUtilities.newArrayList();
        assertEquals(expectedList, StringUtilities.tokenise(str));
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleTokeniseWithNullStr(){
        String str = null;
        StringUtilities.tokenise(str);
    }
}
