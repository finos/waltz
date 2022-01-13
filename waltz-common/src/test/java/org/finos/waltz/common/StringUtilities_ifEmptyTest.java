package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilities_ifEmptyTest {
    @Test
    public void simpleIfEmpty(){
        assertEquals("a", StringUtilities.ifEmpty("a","default"));
    }

    @Test
    public void ifEmptyWithEmptyString(){
        assertEquals("default", StringUtilities.ifEmpty("","default"));
    }

    @Test
    public void ifEmptyWithNullDef(){
        assertEquals(null, StringUtilities.ifEmpty("",null));
    }

    @Test
    public void ifEmptyWithNullString(){
        assertEquals("default", StringUtilities.ifEmpty(null,"default"));
    }

    @Test
    public void ifEmptyWithEmptyDef(){
        assertEquals("", StringUtilities.ifEmpty(null,""));
    }

    @Test
    public void ifEmptyWithBothEmpty(){
        assertEquals("", StringUtilities.ifEmpty("",""));
    }

    @Test
    public void ifEmptyWithBothNull(){
        assertEquals(null, StringUtilities.ifEmpty(null,null));
    }
}
