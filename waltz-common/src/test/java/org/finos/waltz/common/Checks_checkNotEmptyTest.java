package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.finos.waltz.common.CollectionUtilities.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Checks_checkNotEmptyTest {

    @Test
    public void sendNull() {
        String element = null;
        assertThrows(IllegalArgumentException.class, ()
                -> Checks.checkNotEmpty(element, "Test"));
    }

    @Test
    public void sendEmpty() {
        String element = "";
        assertThrows(IllegalArgumentException.class, ()
                -> Checks.checkNotEmpty(element, "Test"));

    }

    @Test
    public void sendElement(){
        String element = "a";
        String result = Checks.checkNotEmpty(element, "Test");
        assertEquals(result, element);
    }
}
