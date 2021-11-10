package org.finos.waltz.common;

import org.junit.Test;

import java.util.*;
import java.util.function.BinaryOperator;

import static java.util.function.Function.identity;
import static org.junit.Assert.assertEquals;

public class MapUtilities_indexBy {

    @Test
    public void simpleIndexBy() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleIndexByWithNulllist() {
        List<String> xs = null;
        Map result = MapUtilities.indexBy(xs, x ->  xs.indexOf(x));
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleIndexByWithNullFunc() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(xs, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleIndexByWithAllNullParams() {
        List<String> xs = null;
        Map result = MapUtilities.indexBy(xs, null);
    }

    @Test
    public void indexByToggledParams() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(x ->  xs.indexOf(x), identity(), xs);
        assertEquals(3, result.size());
        assertEquals("aa", result.get(0));
        assertEquals("bb", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByToggledParamsWithNullList() {
        List<String> xs = null;
        Map result = MapUtilities.indexBy(x ->  xs.indexOf(x), identity(), xs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByToggledParamsWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map result = MapUtilities.indexBy(null, identity(), xs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByToggledParamsWithNullValFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");
        Map result = MapUtilities.indexBy(x -> xs.indexOf(x), null, xs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByToggledParamsWithAllNullParams() {
        List<String> xs = null;
        Map result = MapUtilities.indexBy(null, null, xs);
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
        Map result = MapUtilities.indexBy(x ->  xs.indexOf(x), identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder()));
        assertEquals(0, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByDifferentParamsWithNullList() {
        List<String> xs = null;
        Map result = MapUtilities.indexBy(x ->  xs.indexOf(x), identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByDifferentParamsWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("bb", "aa", "b" );
        Map result = MapUtilities.indexBy(null, identity(), xs, BinaryOperator.maxBy(Comparator.naturalOrder()));
    }

   @Test(expected = NullPointerException.class)
    public void indexByDifferentParamsWithNullBinFn() {
        List<String> xs = ListUtilities.newArrayList("bb", "aa", "b" );
        Map result = MapUtilities.indexBy(x ->  xs.indexOf(x), identity(), xs, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexByDifferentParamsWithAllNullParams() {
        List<String> xs = null;
        Map result = MapUtilities.indexBy(null, null, xs, null);
    }
}
