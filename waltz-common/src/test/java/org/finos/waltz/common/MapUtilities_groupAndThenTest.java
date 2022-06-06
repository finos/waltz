package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.groupAndThen;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapUtilities_groupAndThenTest {
    @Test
    public void simpleGroupAndThen() {
        List<String> xs = newArrayList("aa", "bb", "b" );
        Map<Object, Collection<String>> result = groupAndThen(xs, xs::indexOf, identity());
        assertEquals(3, result.size());
        assertEquals(newArrayList("aa"), result.get(0));
        assertEquals(newArrayList("bb"), result.get(1));
        assertEquals(newArrayList("b"), result.get(2));
    }


    @Test
    public void groupAndThenWithNullList() {
        assertThrows(IllegalArgumentException.class,
                () -> groupAndThen(null, x -> x, identity()));
    }


    @Test
    public void groupAndThenWithNullValueFn() {
        assertThrows(IllegalArgumentException.class,
                () -> groupAndThen(emptyList(), x -> x, null));
    }


    @Test
    public void groupAndThenWithNullKeyFn() {
        assertThrows(IllegalArgumentException.class,
                () -> groupAndThen(emptyList(), null, identity()));
    }


    @Test
    public void groupAndThenWithAllNull() {
        assertThrows(IllegalArgumentException.class,
                () -> groupAndThen(null, null, null));
    }


    @Test
    public void groupAndThenWithEmptyList() {
        Map<Object, Collection<String>> result = groupAndThen(emptyList(), x ->  x, identity());
        assertEquals(0, result.size());
    }
}
