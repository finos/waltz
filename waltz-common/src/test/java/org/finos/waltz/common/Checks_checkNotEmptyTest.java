package org.finos.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Checks_checkNotEmptyTest {

    @Test(expected=IllegalArgumentException.class)
    public void sendNull(){
        String element = null;
        Checks.checkNotEmpty(element, "Test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void sendEmpty(){
        String element = "";
        Checks.checkNotEmpty(element, "Test");
    }

    public void sendElement(){
        String element = "a";
        String result = Checks.checkNotEmpty(element, "Test");
        assertEquals(result, element);
    }
}
