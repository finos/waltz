package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapUtilities_groupAndThenTest {
    @Test
    public void simpleGroupAndThen() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        Map<Object, Collection<String>> result = MapUtilities.groupAndThen(x ->  xs.indexOf(x), identity(), xs);
        assertEquals(3, result.size());
        assertEquals(ListUtilities.newArrayList("aa"), result.get(0));
        assertEquals(ListUtilities.newArrayList("bb"), result.get(1));
        assertEquals(ListUtilities.newArrayList("b"), result.get(2));
    }

    @Test
    public void groupAndThenWithNullList() {
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupAndThen(x -> x, identity(), null));
    }

    @Test
    public void groupAndThenWithNullValueFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupAndThen(x -> x, null, xs));
    }

    @Test
    public void groupAndThenWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b");
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupAndThen(null, identity(), xs));
    }

    @Test
    public void groupAndThenWithAllNull() {
        List<String> xs = null;
        assertThrows(IllegalArgumentException.class,
                () -> MapUtilities.groupAndThen(null, null, xs));
    }

    @Test
    public void groupAndThenWithEmptyList() {
        List<String> xs = ListUtilities.newArrayList();
         Map<Object, Collection<String>> result = MapUtilities.groupAndThen(x ->  x, identity(), xs);
        assertEquals(0, result.size());

    }
}
