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

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.CollectionUtilities.find;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static org.junit.Assert.assertEquals;

public class CollectionUtilities_find {

    private static final List<String> words = newArrayList(
            "a",
            "hello",
            "world");


    @Test(expected = IllegalArgumentException.class)
    public void predicateCannotBeNull() {
        find(null, words);
    }


    @Test(expected = IllegalArgumentException.class)
    public void collectionCannotBeNull() {
        find(d -> true, null);
    }


    @Test
    public void canFindThings() {
        Optional<String> r = find(
                d -> d.contains("e"),
                words);
        assertEquals(Optional.of("hello"), r);
    }


    @Test
    public void emptyReturnedIfNotFound() {
        Optional<String> r = find(
                d -> d.contains("z"),
                words);
        assertEquals(Optional.empty(), r);
    }

}
