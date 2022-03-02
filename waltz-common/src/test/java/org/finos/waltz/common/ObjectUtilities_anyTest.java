package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectUtilities_anyTest {

    @Test
    public void simpleAny() {
        String[] ele = {"a", "bb"};
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertTrue(result);
    }

    @Test
    public void simpleAnyNoMatch() {
        String[] ele = {"aa", "bb"};
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertFalse(result);
    }

    @Test
    public void simpleAnyEmptyArray() {
        String[] ele = {};
        boolean result = ObjectUtilities.any(x->x.length()==1, ele);
        assertFalse(result);
    }

    @Test
    public void simpleAnyNullArray() {
        String[] ele = null;
        assertThrows(NullPointerException.class,
                () -> ObjectUtilities.any(x -> x.length() == 1, ele));

    }

    @Test
    public void simpleAnyNullPredicate() {
        String[] ele = {"aa", "bb"};
        assertThrows(NullPointerException.class,
                () -> ObjectUtilities.any(null, ele));
    }

    @Test
    public void simpleAnyAllNull() {
        String[] ele = null;
        assertThrows(NullPointerException.class,
                () -> ObjectUtilities.any(null, ele));

    }
}
