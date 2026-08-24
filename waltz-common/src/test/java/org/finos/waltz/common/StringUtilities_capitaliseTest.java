package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_capitaliseTest {

    @Test
    public void allUpperCase(){
        assertEquals("Something", StringUtilities.capitalise("SOMETHING"));
    }

    @Test
    public void multipleAllUpperCase(){
        assertEquals("Hello World", StringUtilities.capitalise("HELLO WORLD"));
    }


    @Test
    public void allLowerCase(){
        assertEquals("Something", StringUtilities.capitalise("something"));
    }

    @Test
    public void multipleAllLowerCase(){
        assertEquals("Hello World", StringUtilities.capitalise("hello world"));
    }

    @Test
    public void nullInput(){
        assertEquals("", StringUtilities.capitalise(null));
    }

    @Test
    public void emptyInput(){
        assertEquals("", StringUtilities.capitalise(""));
    }

    @Test
    public void tabInput(){
        // "      " => ""
        // "Hello\tWorld" =>
        assertEquals("Hello World", StringUtilities.capitalise("HELLO\tWORLD"));
    }

    @Test
    public void leadingWhitespace(){
        // Leading whitespace yields an empty leading token that must not throw
        assertEquals("Hello", StringUtilities.capitalise(" hello"));
        assertEquals("Hello World", StringUtilities.capitalise("  hello world"));
    }

    @Test
    public void onlyWhitespace(){
        assertEquals("", StringUtilities.capitalise("   "));
    }

    public void newLineInputWithTab(){
        // "      " => ""
        // "Hello\tWorld" =>
        assertEquals("Hello\nWorld", StringUtilities.capitalise("HELLO \t\n WORLD"));
    }
}
