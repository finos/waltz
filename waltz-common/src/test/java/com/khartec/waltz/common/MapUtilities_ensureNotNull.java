package com.khartec.waltz.common;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapUtilities_ensureNotNull {

    @Test
    public void simpleEnsureNotNull() {
        Map<Integer, String> xs = MapUtilities.newHashMap(1, "aa");
        Map<Integer, String> result = MapUtilities.ensureNotNull(xs);
        assertEquals(1, result.size());
        assertEquals("aa", result.get(1));
    }

    @Test
    public void simpleEnsureNotNullWithNullMap() {
        Map<Integer, String> xs = null;
        Map<Integer, String> result = MapUtilities.ensureNotNull(xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void simpleEnsureNotNullWithEmptyMap() {
        Map<Integer, String> xs = MapUtilities.newHashMap();
        Map<Integer, String> result = MapUtilities.ensureNotNull(xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }
}
