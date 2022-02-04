package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void simpleUnionWithOneNullColl() {
        Collection<String> coll1 = ListUtilities.newArrayList("a");
        Collection<String> coll2 = null;
        assertThrows(NullPointerException.class,
                () -> SetUtilities.union(coll1, coll2));
    }

    @Test
    public void simpleUnionWithTwoEmptyColl(){
        Collection<String> coll1 = ListUtilities.newArrayList();
        Collection<String> coll2 = ListUtilities.newArrayList();
        Set<String> result = SetUtilities.union(coll1,coll2);
        assertEquals(0,result.size());
    }

    @Test
    public void simpleUnionWithTwoNullColl() {
        Collection<String> coll1 = null;
        Collection<String> coll2 = null;
        assertThrows(NullPointerException.class,
                () -> SetUtilities.union(coll1, coll2));
    }

}
