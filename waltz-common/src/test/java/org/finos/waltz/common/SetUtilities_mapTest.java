package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetUtilities_mapTest {
    @Test
    public void simpleMap(){
        Collection<String> coll = ListUtilities.newArrayList("a","aa","b");
        Set result = SetUtilities.map(coll, x->x.length());
        assertEquals(2,result.size());
        assertEquals(1,result.toArray()[0]);
        assertEquals(2,result.toArray()[1]);
    }

    @Test
    public void simpleMapWithEmptyColl(){
        Collection coll = ListUtilities.newArrayList();
        Set result = SetUtilities.map(coll, x->x);
        assertEquals(0,result.size());
    }

    @Test
    public void simpleMapWithNullColl(){
        Collection coll = null;
        Set result = SetUtilities.map(coll, x->x);
        assertEquals(0,result.size());
    }

    @Test
    public void simpleMapWithAllNull(){
        Collection coll = null;
        Set result = SetUtilities.map(coll, null);
        assertEquals(0,result.size());
    }
}
