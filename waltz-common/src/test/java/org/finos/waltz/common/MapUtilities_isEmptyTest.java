package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapUtilities_isEmptyTest {

    @Test
    public void simpleIsEmpty() {
        Map<Integer, String> xs = MapUtilities.newHashMap(1, "aa");
        boolean result = MapUtilities.isEmpty( xs );
        assertEquals(false, result);
    }

    @Test
    public void simpleIsEmptyWithEmptyMap() {
        Map<Integer, String> xs = MapUtilities.newHashMap();
        boolean result = MapUtilities.isEmpty( xs );
        assertEquals(true, result);
    }

    @Test
    public void simpleIsEmptyWithNullMap() {
        Map<Integer, String> xs = null;
        boolean result = MapUtilities.isEmpty( xs );
        assertEquals(true, result);
    }
}
