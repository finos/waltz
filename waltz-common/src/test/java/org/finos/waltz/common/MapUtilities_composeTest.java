package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MapUtilities_composeTest {

    @Test
    public void simpleCompose() {
        Map map1 = MapUtilities.newHashMap(1,1,2,2);
        Map map2 = MapUtilities.newHashMap(1,'a',2,'b');
        Map<Integer, Character> result = MapUtilities.compose(map1, map2);
        assertEquals(2, result.size());
        assertEquals(java.util.Optional.of('a'), java.util.Optional.ofNullable(result.get(1)));
        assertEquals(java.util.Optional.of('b'), java.util.Optional.ofNullable(result.get(2)));
    }

    @Test
    public void simpleComposeWithUnequalMaps() {
        Map map1 = MapUtilities.newHashMap(1,1,2,2);
        Map map2 = MapUtilities.newHashMap(1,'a');
        Map<Integer, Character> result = MapUtilities.compose(map1, map2);
        assertEquals(2, result.size());
        assertEquals(java.util.Optional.of('a'), java.util.Optional.ofNullable(result.get(1)));
        assertEquals(Optional.empty(), java.util.Optional.ofNullable(result.get(2)));
    }

    @Test
    public void simpleComposeWithEmptyMap1() {
        Map map1 = MapUtilities.newHashMap();
        Map map2 = MapUtilities.newHashMap(1,'a',2,'b');
        Map<Integer, Character> result = MapUtilities.compose(map1, map2);
        assertEquals(0, result.size());
    }

    @Test
    public void simpleComposeWithNullMap1() {
        Map map1 = null;
        Map map2 = MapUtilities.newHashMap(1, 'a', 2, 'b');
        assertThrows(NullPointerException.class,
                () -> MapUtilities.compose(map1, map2));
    }

    @Test
    public void simpleComposeWithEmptyMap2() {
        Map map1 = MapUtilities.newHashMap(1,1,2,2);
        Map map2 = MapUtilities.newHashMap();
        Map<Integer, Character> result = MapUtilities.compose(map1, map2);
        assertEquals(2, result.size());
        assertEquals(Optional.empty(), java.util.Optional.ofNullable(result.get(1)));
        assertEquals(Optional.empty(), java.util.Optional.ofNullable(result.get(2)));
    }

    @Test
    public void simpleComposeWithNullMap2() {
        Map map1 = MapUtilities.newHashMap(1, 1, 2, 2);
        Map map2 = null;
        assertThrows(NullPointerException.class,
                () -> MapUtilities.compose(map1, map2));
    }

    @Test
    public void simpleComposeWithAllEmptyMaps() {
        Map map1 = MapUtilities.newHashMap();
        Map map2 = MapUtilities.newHashMap();
        Map<Integer, Character> result = MapUtilities.compose(map1, map2);
        assertEquals(0, result.size());
    }

    @Test
    public void simpleComposeWithAllNullMaps() {
        Map map1 = null;
        Map map2 = null;
        assertThrows(NullPointerException.class,
                () -> MapUtilities.compose(map1, map2));
    }
}
