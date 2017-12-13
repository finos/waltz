/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class MapUtilities_groupBy {

    @Test
    public void simpleGroupBy() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b" , "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), xs);
        assertEquals(3, result.size());

        // 2 of length 2
        assertEquals(2, result.get(2).size());
        // 1 of length 1
        assertEquals(1, result.get(1).size());
        // 1 of length 3
        assertEquals(1, result.get(3).size());
    }

    @Test
    public void canTransformValues() {
        List<String> xs = ListUtilities.newArrayList("aa", "bb", "b", "ccc");
        Map<Integer, Collection<String>> result = MapUtilities.groupBy(x -> x.length(), x -> x.toUpperCase(), xs);
        assertEquals(3, result.size());
        String b = CollectionUtilities.first(result.get(1));
        assertEquals("B", b);
    }
}
