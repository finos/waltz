package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapUtilities_ensureNotNullTest {

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
