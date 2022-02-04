package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilities_joinTest {

    @Test
    public void simpleJoinWithAlpha(){
        Collection coll = ListUtilities.newArrayList("a","b","c");
        assertEquals("a,b,c", StringUtilities.join(coll,","));
    }

    @Test
    public void simpleJoinWithNum(){
        Collection coll = ListUtilities.newArrayList(1, 2, 3);
        assertEquals("1,2,3", StringUtilities.join(coll,","));
    }

    @Test
    public void simpleJoinWithEmptySep(){
        Collection coll = ListUtilities.newArrayList("a","b","c");
        assertEquals("abc", StringUtilities.join(coll,""));
    }

    @Test
    public void simpleJoinWithEmptyColl(){
        Collection coll = ListUtilities.newArrayList();
        assertEquals("", StringUtilities.join(coll,","));
    }

    @Test
    public void simpleJoinWithNullColl() {
        Collection coll = null;

        assertThrows(NullPointerException.class,
                () -> StringUtilities.join(coll, ","));
    }

    @Test
    public void simpleJoinWithBothEmpty(){
        Collection coll = ListUtilities.newArrayList();
        assertEquals("", StringUtilities.join(coll,""));
    }

    @Test
    public void simpleJoinWithBothNull() {
        Collection coll = null;

        assertThrows(NullPointerException.class,
                () -> StringUtilities.join(coll, null));
    }
}
