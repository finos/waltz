package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_toOptionalTest {

    @Test
    public void simpleToOptional(){
        String str = "abc";
        assertEquals(Optional.of("abc"), StringUtilities.toOptional(str));
    }

    @Test
    public void simpleToOptionalWithEmptyStr(){
        String str = "";
        assertEquals(Optional.empty(), StringUtilities.toOptional(str));
    }

    @Test
    public void simpleToOptionalWithNullStr(){
        String str = null;
        assertEquals(Optional.empty(), StringUtilities.toOptional(str));
    }
}
