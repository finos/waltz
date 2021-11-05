package org.finos.waltz.common;

import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithNullList() {
        MapUtilities.groupAndThen(x ->  x, identity(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithNullValueFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        MapUtilities.groupAndThen(x ->  x, null, xs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        MapUtilities.groupAndThen(null, identity(), xs);
    }

   @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithAllNull() {
        List<String> xs = null;
        MapUtilities.groupAndThen(null, null, xs);
    }

     @Test
    public void groupAndThenWithEmptyList() {
        List<String> xs = ListUtilities.newArrayList();
         Map<Object, Collection<String>> result = MapUtilities.groupAndThen(x ->  x, identity(), xs);
        assertEquals(0, result.size());
    }
}
