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

    public void newLineInputWithTab(){
        // "      " => ""
        // "Hello\tWorld" =>
        assertEquals("Hello\nWorld", StringUtilities.capitalise("HELLO \t\n WORLD"));
    }
}
