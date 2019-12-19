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

import java.util.Collections;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.common.SetUtilities.intersection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilities_intersection {

    @Test
    public void intersectionOfEmptySetsGivesEmptySet() {
        assertTrue(intersection(Collections.emptySet(), Collections.emptySet()).isEmpty());
    }


    @Test
    public void intersectionOfDisjointSetsGivesEmptySet() {
        assertTrue(intersection(asSet("a", "b"), asSet("x", "y")).isEmpty());
    }


    @Test
    public void intersectionOfSetsGivesOnlyElementsThatAreInBoth() {
        assertEquals(
                "partial intersection",
                asSet("b"),
                intersection(asSet("a", "b"), asSet("b", "c")));

        assertEquals(
                "total intersection",
                asSet("a", "b"),
                intersection(asSet("a", "b"), asSet("b", "c", "a")));
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotIntersectNulls_both() {
        intersection(null, null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotIntersectNulls_first() {
        intersection(null, Collections.emptySet());
    }


    @Test(expected = IllegalArgumentException.class)
    public void cannotIntersectNulls_second() {
        intersection(Collections.emptySet(), null);
    }

}
