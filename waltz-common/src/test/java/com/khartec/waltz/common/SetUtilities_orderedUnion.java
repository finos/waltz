package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.orderedUnion;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_orderedUnion {

    private Set<String> abcSet = asSet("a", "b", "c");
    private Set<String> bcaSet = asSet("b", "c", "a");
    private Set<String> bcdSet = asSet("b", "c", "d");
    private Set<String> abcdefSet = asSet("a", "b", "c", "d", "e", "f");
    private Set<String> defSet = asSet("d", "e", "f");
    private Set<String> efaSet = asSet( "e", "f", "a");


    @Test
    public void unionTwoEmptySetReturnEmptySet(){
        assertTrue(orderedUnion(emptySet(), emptySet()).isEmpty());
    }


    @Test
    public void unionEmptySetWithNotEmptySetReturnsSet(){
        assertEquals(abcSet, orderedUnion(abcSet, emptySet()));
        assertEquals(abcSet, orderedUnion(bcaSet, emptySet()));
    }


    @Test
    public void unionTwoSetsReturnsOrderedSetOfAllElements(){
        assertEquals(abcdefSet, orderedUnion(abcSet, defSet));
        assertEquals(abcdefSet, orderedUnion(defSet, abcSet));
        assertEquals(6, orderedUnion(abcSet, defSet).size());
        assertEquals(asSet("a", "b", "c", "d"), orderedUnion(bcdSet, abcSet));
        assertEquals(asSet("a", "d", "e", "f"), orderedUnion(efaSet, defSet));
    }
}
