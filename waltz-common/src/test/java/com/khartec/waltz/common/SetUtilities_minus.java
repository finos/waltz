package com.khartec.waltz.common;

import org.junit.Test;

import java.util.Set;

import static com.khartec.waltz.common.SetUtilities.*;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_minus {

    private Set<String> abcSet = asSet("a", "b", "c");
    private Set<String> abcdefSet = asSet("a", "b", "c", "d", "e", "f");
    private Set<String> bcdSet = asSet("b", "c", "d");
    private Set<String> defSet = asSet("d", "e", "f");


    @Test
    public void emptySetMinusEmptySetIsEmpty() {
        assertTrue("Empty set minus empty set should be an empty set",
                minus(emptySet(), emptySet()).isEmpty());
    }


    @Test
    public void setMinusItselfIsEmpty() {
        assertTrue("Set minus itself should be an empty set", minus(abcSet, abcSet).isEmpty());
    }


    @Test
    public void twoNonIntersectingSetsReturnFirstSet() {
        assertTrue(intersection(abcSet, defSet).isEmpty());
        assertEquals(abcSet, minus(abcSet, defSet));
    }


    @Test
    public void minusRemovesAllLaterSetElementsFromFirstSet(){
        assertEquals(asSet("a"), minus(abcSet, bcdSet));
        assertEquals(asSet("d"), minus(bcdSet, abcSet));
        assertEquals(asSet("a"), minus(abcdefSet, bcdSet, defSet));
        assertEquals(3, minus(abcdefSet, abcSet).size());
        assertEquals(emptySet(), minus(abcSet, bcdSet, abcdefSet));
    }

}
