package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class StringUtilities_join {

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

    @Test(expected = NullPointerException.class)
    public void simpleJoinWithNullColl(){
        Collection coll = null;
        StringUtilities.join(coll,",");
    }

    @Test
    public void simpleJoinWithBothEmpty(){
        Collection coll = ListUtilities.newArrayList();
        assertEquals("", StringUtilities.join(coll,""));
    }

    @Test(expected = NullPointerException.class)
    public void simpleJoinWithBothNull(){
        Collection coll = null;
        assertEquals("", StringUtilities.join(coll,null));
    }
}
