/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
