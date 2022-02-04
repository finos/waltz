package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapUtilities_newHashMapTest {

    @Test
    public void emptyHashMap(){
        Map m = MapUtilities.newHashMap();
        assertEquals(0, m.size());
        assertTrue(m.isEmpty());
    }

    @Test
    public void SingleElementHashMap(){
        Map m = MapUtilities.newHashMap(1, "a");
        assertEquals(1, m.size());
        assertEquals("a", m.get(1));
    }

    @Test
    public void TwoElementsHashMap(){
        Map m = MapUtilities.newHashMap(1, "a",2, "b");
        assertEquals(2, m.size());
        assertEquals("a", m.get(1));
        assertEquals("b", m.get(2));
    }

    @Test
    public void ThreeElementsHashMap(){
        Map m = MapUtilities.newHashMap(1, "a",2, "b", 3, "c");
        assertEquals(3, m.size());
        assertEquals("a", m.get(1));
        assertEquals("b", m.get(2));
        assertEquals("c", m.get(3));
    }

    @Test
    public void FourElementsHashMap(){
        Map m = MapUtilities.newHashMap(1, "a",2, "b", 3, "c", 4, "d");
        assertEquals(4, m.size());
        assertEquals("a", m.get(1));
        assertEquals("b", m.get(2));
        assertEquals("c", m.get(3));
        assertEquals("d", m.get(4));
    }

    @Test
    public void FiveElementsHashMap(){
        Map m = MapUtilities.newHashMap(1, "a",2, "b", 3, "c", 4, "d", 5, "e");
        assertEquals(5, m.size());
        assertEquals("a", m.get(1));
        assertEquals("b", m.get(2));
        assertEquals("c", m.get(3));
        assertEquals("d", m.get(4));
        assertEquals("e", m.get(5));
    }

    @Test
    public void SixElementsHashMap(){
        Map m = MapUtilities.newHashMap(1, "a",2, "b", 3, "c", 4, "d", 5, "e", 6, "f");
        assertEquals(6, m.size());
        assertEquals("a", m.get(1));
        assertEquals("b", m.get(2));
        assertEquals("c", m.get(3));
        assertEquals("d", m.get(4));
        assertEquals("e", m.get(5));
        assertEquals("f", m.get(6));
    }
}
