package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilities_tokeniseTest {

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

    @Test
    public void simpleTokeniseWithNullStr() {
        String str = null;

        assertThrows(IllegalArgumentException.class,
                () -> StringUtilities.tokenise(str));
    }
}
