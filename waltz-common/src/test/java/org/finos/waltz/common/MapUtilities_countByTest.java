package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MapUtilities_countByTest {

    @Test
    public void simpleCountBy() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" , "ccc");
        Map<Object, Long> result = MapUtilities.countBy(x -> x.length(), xs);
        assertEquals(3, result.size());
        assertEquals(1, result.get(1).longValue());
        assertEquals(2, result.get(2).longValue());
        assertEquals(1, result.get(3).longValue());
    }

    @Test
    public void simpleCountByWithEmptyList() {
        List<String> xs = new ArrayList();
        Map<Object, Long> result = MapUtilities.countBy(x -> x.length(), xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void simpleCountByWithNullList() {
        List<String> xs = null;
        Map<Object, Long> result = MapUtilities.countBy(x -> x.length(), xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    public void simpleCountByWithNullFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        assertThrows(NullPointerException.class,
                () -> MapUtilities.countBy(null, xs));
    }

    @Test
    public void simpleCountByWithAllNullParams() {
        List<String> xs = null;
        Map<Object, Long> result = MapUtilities.countBy(null, xs);
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }
}
