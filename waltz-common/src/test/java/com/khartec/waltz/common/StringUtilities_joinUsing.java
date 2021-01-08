package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class StringUtilities_joinUsing {

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

    @Test(expected = NullPointerException.class)
    public void simpleJoinUsingWithNullColl(){
        Collection coll = null;
        StringUtilities.joinUsing(coll, x->x.toString()+x.toString(),",");
    }

    @Test
    public void simpleJoinUsingWithTwoEmpty(){
        Collection coll = ListUtilities.newArrayList();
        assertEquals("", StringUtilities.joinUsing(coll, x->x.toString()+x.toString(),""));
    }

    @Test(expected = NullPointerException.class)
    public void simpleJoinUsingWithTwoNull(){
        Collection coll = null;
       StringUtilities.joinUsing(coll, x->x.toString()+x.toString(),null);
    }

    @Test(expected = NullPointerException.class)
    public void simpleJoinUsingWithAllNull(){
        Collection coll = null;
        StringUtilities.joinUsing(coll, null,null);
    }
}
