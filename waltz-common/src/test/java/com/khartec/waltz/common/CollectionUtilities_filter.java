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

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.CollectionUtilities.filter;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_filter {

    private static final List<String> words = newArrayList(
            "hello",
            "world");


    @Test
    public void filteringOutAllElementsGivesEmptyCollection() {
        assertTrue(filter(words, w -> w.startsWith("z"))
                .isEmpty());
    }


    @Test
    public void filteringWorks() {
        Collection<String> wordsStartingWithH = filter(words, w -> w.startsWith("h"));
        assertEquals(1, wordsStartingWithH.size());
        assertTrue(wordsStartingWithH.contains("hello"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void filteringANullCollectionThrowsIllegalArgException() {
        filter(null, x -> true);
    }


    @Test(expected = IllegalArgumentException.class)
    public void filteringWithoutAFilterPredicatedThrowsIllegalArgumentException() {
        filter(words, null);
    }

}
