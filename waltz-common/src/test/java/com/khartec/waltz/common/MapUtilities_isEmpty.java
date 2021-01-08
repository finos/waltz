package com.khartec.waltz.common;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MapUtilities_isEmpty {

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
