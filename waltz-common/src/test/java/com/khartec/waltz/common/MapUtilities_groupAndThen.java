package com.khartec.waltz.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static org.junit.Assert.assertEquals;

public class MapUtilities_groupAndThen {
    @Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map<Object, Collection<String>> result = MapUtilities.groupAndThen(x ->  xs.indexOf(x), identity(), xs);
        assertEquals(3, result.size());
        assertEquals(ListUtilities.newArrayList("aa"), result.get(0));
        assertEquals(ListUtilities.newArrayList("bb"), result.get(1));
        assertEquals(ListUtilities.newArrayList("b"), result.get(2));
    }

    /*@Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }*/
}
