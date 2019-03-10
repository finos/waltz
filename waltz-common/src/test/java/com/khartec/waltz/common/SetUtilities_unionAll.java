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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.SetUtilities.unionAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_unionAll {

    @Test
    public void unionAllOfNothingGivesEmptySet() {
        assertTrue(unionAll(Collections.emptyList()).isEmpty());
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotUnionAllNull() {
        unionAll(null);
    }


    @Test
    public void unionAllTakesACollectionOfCollectionsAndReturnsTheUnionOfAllSubCollections() {
        ArrayList<String> listA = newArrayList("a", "b");
        ArrayList<String> listB = newArrayList("b", "c");
        Collection<String> r = unionAll(newArrayList(listA, listB));
        assertTrue(! r.isEmpty());
        assertEquals(3, r.size());
        assertTrue(r.contains("a"));
        assertTrue(r.contains("b"));
        assertTrue(r.contains("c"));
    }

}
