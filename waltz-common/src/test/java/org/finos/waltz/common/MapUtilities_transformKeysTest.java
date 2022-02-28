package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MapUtilities_transformKeysTest {

    @Test
    public void simpleTransformKeys() {
        Map original = MapUtilities.newHashMap(1,'a',2,'b');
        Map<Integer, Character> result = MapUtilities.transformKeys(original, x -> Integer.parseInt(x.toString())*2);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(2));
        assertTrue(result.containsKey(4));
    }

    @Test
    public void simpleTransformKeysWithNullMap() {
        Map original = null;
        assertThrows(NullPointerException.class,
                () -> MapUtilities.transformKeys(original, x -> Integer.parseInt(x.toString()) * 2));
    }

    @Test
    public void simpleTransformKeysWithNullFunction() {
        Map original = MapUtilities.newHashMap(1, 'a', 2, 'b');
        assertThrows(NullPointerException.class,
                () -> MapUtilities.transformKeys(original, null));
    }

    @Test
    public void simpleTransformKeysWithAllNull() {
        Map original = null;
        assertThrows(NullPointerException.class,
                () -> MapUtilities.transformKeys(original, null));
    }

    @Test
    public void simpleTransformKeysWithEmptyMap() {
        Map original = MapUtilities.newHashMap();
        Map<Integer, Character> result = MapUtilities.transformKeys(original, x -> Integer.parseInt(x.toString())*2);
        assertEquals(0, result.size());
    }
}
