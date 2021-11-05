package org.finos.waltz.common;

import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class SetUtilities_uniqBy {
    @Test
    public void simpleUniqByWithMultiElements() throws NullPointerException{
        Collection<String> ele = ListUtilities.newArrayList("a","a","b","cc");
        Set result = SetUtilities.uniqBy(ele, x->x);
        assertEquals(3, result.size());
        assertEquals("cc", result.toArray()[0]);
        assertEquals("a", result.toArray()[1]);
        assertEquals("b", result.toArray()[2]);
    }

    @Test
    public void simpleUniqByWithOneElement(){
        Collection<String> ele = ListUtilities.newArrayList("a");
        Set result =SetUtilities.uniqBy(ele, x->x);
        assertEquals(1, result.size());
        assertEquals("a", result.toArray()[0]);
    }

    @Test
    public void simpleUniqByWithNoElement(){
        Collection<String> ele = ListUtilities.newArrayList();
        Set result =SetUtilities.uniqBy(ele, x->x);
        assertEquals(0, result.size());
    }

    @Test(expected = NullPointerException.class)
    public void simpleUniqByWithNullElement(){
        Collection<String> ele = null;
        SetUtilities.uniqBy(ele, x->x);
    }

}
