package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithNullList() {
        MapUtilities.groupAndThen(x ->  x, identity(), null);
        /*

        assertThrows(NullPointerException.class,
                ()->  StringUtilities.join(coll,","));
         */
    }

    @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithNullValueFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        MapUtilities.groupAndThen(x ->  x, null, xs);
        /*

        assertThrows(NullPointerException.class,
                ()->  StringUtilities.join(coll,","));
         */
    }

    @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithNullKeyFn() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" );
        MapUtilities.groupAndThen(null, identity(), xs);
        /*

        assertThrows(NullPointerException.class,
                ()->  StringUtilities.join(coll,","));
         */
    }

   @Test(expected = IllegalArgumentException.class)
    public void groupAndThenWithAllNull() {
        List<String> xs = null;
        MapUtilities.groupAndThen(null, null, xs);
        /*

        assertThrows(NullPointerException.class,
                ()->  StringUtilities.join(coll,","));
         */
    }

     @Test
    public void groupAndThenWithEmptyList() {
        List<String> xs = ListUtilities.newArrayList();
         Map<Object, Collection<String>> result = MapUtilities.groupAndThen(x ->  x, identity(), xs);
        assertEquals(0, result.size());

    }
}
