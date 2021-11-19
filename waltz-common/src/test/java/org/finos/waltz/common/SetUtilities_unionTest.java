package org.finos.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SetUtilities_unionTest {

    @Test
    public void simpleUnionTwoCollection(){
        Collection<String> coll1 = ListUtilities.newArrayList("a","b");
        Collection<String> coll2 = ListUtilities.newArrayList("a");
        Set<String> result = SetUtilities.union(coll1,coll2);
        assertEquals(2,result.size());
        assertEquals("a",result.toArray()[0]);
        assertEquals("b",result.toArray()[1]);
    }

    @Test
    public void simpleUnionWithOneEmptyColl(){
        Collection<String> coll1 = ListUtilities.newArrayList("a");
        Collection<String> coll2 = ListUtilities.newArrayList();
        Set<String> result = SetUtilities.union(coll1,coll2);
        assertEquals(1,result.size());
        assertEquals("a",result.toArray()[0]);
    }

    @Test(expected = NullPointerException.class)
    public void simpleUnionWithOneNullColl(){
        Collection<String> coll1 = ListUtilities.newArrayList("a");
        Collection<String> coll2 = null;
        SetUtilities.union(coll1,coll2);
    }

    @Test
    public void simpleUnionWithTwoEmptyColl(){
        Collection<String> coll1 = ListUtilities.newArrayList();
        Collection<String> coll2 = ListUtilities.newArrayList();
        Set<String> result = SetUtilities.union(coll1,coll2);
        assertEquals(0,result.size());
    }

    @Test(expected = NullPointerException.class)
    public void simpleUnionWithTwoNullColl(){
        Collection<String> coll1 = null;
        Collection<String> coll2 = null;
        SetUtilities.union(coll1,coll2);
    }

}
