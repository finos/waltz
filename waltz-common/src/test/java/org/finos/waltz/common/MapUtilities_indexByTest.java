package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BinaryOperator;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapUtilities_indexByTest {

    @Test
    public void simpleIndexBy() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void simpleIndexByWithNulllist() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(xs, x -> xs.indexOf(x)));

    }

    @Test
    public void simpleIndexByWithNullFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(xs, null));
    }

    @Test
    public void simpleIndexByWithAllNullParams() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(xs, null));
    }

    @Test
    public void indexByToggledParams() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");
        Map result = MapUtilities.indexBy(xs::indexOf, identity(), xs);
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void indexByToggledParamsWithNullList() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(x -> xs.indexOf(x), identity(), xs));
    }

    @Test
    public void indexByToggledParamsWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(null, identity(), xs));
    }

    @Test
    public void indexByToggledParamsWithNullValFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");

        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(xs::indexOf, null, xs));
    }

    @Test
    public void indexByToggledParamsWithAllNullParams() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(null, null, xs));
    }

    @Test
    public void indexByDifferentParams() {
        List<String> xs = ListUtilities.newArrayList("bb", "aa", "b" );
        Map result = MapUtilities.indexBy(x ->  xs.indexOf(x), identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder()));
        assertEquals(3, result.size());
        assertEquals("bb", result.get(0));
        assertEquals("aa", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    public void indexByDifferentParamsWithEmptyList() {
        List<String> xs = new ArrayList();
        Map result = MapUtilities.indexBy(xs::indexOf, identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder()));
        assertEquals(0, result.size());
    }

    @Test
    public void indexByDifferentParamsWithNullList() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(x -> xs.indexOf(x), identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder())));
    }

    @Test
    public void indexByDifferentParamsWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("bb", "aa", "b");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(null, identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder())));
    }

    @Test
    public void indexByDifferentParamsWithNullBinFn() {
        List<String> xs = ListUtilities.newArrayList("bb", "aa", "b");
        assertThrows(NullPointerException.class,
                () -> MapUtilities.indexBy(x -> xs.indexOf(x), identity(), xs, null));
    }

    @Test
    public void indexByDifferentParamsWithAllNullParams() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.indexBy(null, null, xs, null));
    }
}
