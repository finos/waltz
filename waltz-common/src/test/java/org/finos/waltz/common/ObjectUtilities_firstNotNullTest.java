package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectUtilities_firstNotNullTest {

    @Test
    public void simpleFirstNotNull() {
     String[] ele = {"a", "b"};
     String result = ObjectUtilities.firstNotNull(ele);
     assertEquals("a", result);
    }

    @Test
    public void firstNotNullWithFirstNull() {
        String[] ele = {null, "b"};
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals("b", result);
    }

    @Test
    public void firstNotNullWithFirstEmpty() {
        String[] ele = {"", "b"};
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals("", result);
    }

    @Test
    public void firstNotNullWithEmptyArray() {
        String[] ele = { };
        String result = ObjectUtilities.firstNotNull(ele);
        assertEquals(null, result);
    }

    @Test
    public void firstNotNullWithNullArray() {
        String[] ele = null;
        assertThrows(NullPointerException.class,
                () -> ObjectUtilities.firstNotNull(ele));
    }
}
