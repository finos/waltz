package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilities_joinUsingTest {

    @Test
    public void simpleJoinUsingWithAlpha(){
        Collection coll = ListUtilities.newArrayList("a","b","c");
        assertEquals("aa,bb,cc", StringUtilities.joinUsing(coll,x->x.toString()+x.toString(),","));
    }

    @Test
    public void simpleJoinUsingWithNum(){
        Collection coll = ListUtilities.newArrayList(1, 2, 3);
        assertEquals("11,22,33", StringUtilities.joinUsing(coll,x->x.toString()+x.toString(),","));
    }

    @Test
    public void simpleJoinUsingWithEmptySep(){
        Collection coll = ListUtilities.newArrayList("a","b","c");
        assertEquals("aabbcc", StringUtilities.joinUsing(coll,x->x.toString()+x.toString(),""));
    }

    @Test
    public void simpleJoinUsingWithEmptyColl(){
        Collection coll = ListUtilities.newArrayList();
        assertEquals("", StringUtilities.joinUsing(coll, x->x.toString()+x.toString(),","));
    }

    @Test
    public void simpleJoinUsingWithNullColl() {
        Collection coll = null;
        assertThrows(NullPointerException.class,
                () -> StringUtilities.joinUsing(coll, x -> x.toString() + x.toString(), ","));
    }

    @Test
    public void simpleJoinUsingWithTwoEmpty(){
        Collection coll = ListUtilities.newArrayList();
        assertEquals("", StringUtilities.joinUsing(coll, x->x.toString()+x.toString(),""));
    }

    @Test
    public void simpleJoinUsingWithTwoNull() {
        Collection coll = null;
        assertThrows(NullPointerException.class,
                () -> StringUtilities.joinUsing(coll, x -> x.toString() + x.toString(), null));
    }

    @Test
    public void simpleJoinUsingWithAllNull() {
        Collection coll = null;
        assertThrows(NullPointerException.class,
                () -> StringUtilities.joinUsing(coll, null, null));
    }
}
