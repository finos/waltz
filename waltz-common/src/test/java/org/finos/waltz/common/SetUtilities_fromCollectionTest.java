package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetUtilities_fromCollectionTest {

    @Test
    public void simpleFromCollection(){
        Collection<String> coll = ListUtilities.newArrayList("a","a","b");
        Set result = SetUtilities.fromCollection(coll);
        assertEquals(2,result.size());
        assertEquals("a",result.toArray()[0]);
        assertEquals("b",result.toArray()[1]);
    }

    @Test
    public void fromCollectionWithEmptyColl(){
        Collection<String> coll = ListUtilities.newArrayList();
        Set result = SetUtilities.fromCollection(coll);
        assertEquals(0,result.size());
    }

    @Test
    public void fromCollectionWithNullColl(){
        Collection<String> coll = null;
        Set result = SetUtilities.fromCollection(coll);
        assertEquals(0,result.size());
    }

}
