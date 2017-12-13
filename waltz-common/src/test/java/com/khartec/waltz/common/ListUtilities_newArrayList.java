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

import java.util.ArrayList;

import static com.khartec.waltz.common.TestUtilities.assertLength;
import static org.junit.Assert.assertEquals;

public class ListUtilities_newArrayList {

    @Test
    public void singleElement() {
        ArrayList<String> arr = ListUtilities.newArrayList("a");
        assertLength(arr, 1);
        assertEquals("a", arr.get(0));
    }

    @Test
    public void multipleElements() {
        ArrayList<String> arr = ListUtilities.newArrayList("a", "b");
        assertLength(arr, 2);
        assertEquals("a", arr.get(0));
        assertEquals("b", arr.get(1));
    }

    @Test
    public void zeroElements() {
        ArrayList<String> arr = ListUtilities.newArrayList();
        assertLength(arr, 0);
    }
}
