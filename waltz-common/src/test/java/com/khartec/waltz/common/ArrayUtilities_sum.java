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

import static junit.framework.TestCase.assertEquals;

public class ArrayUtilities_sum {

    @Test(expected = IllegalArgumentException.class)
    public void cannotSumNull() {
        ArrayUtilities.sum(null);
    }


    @Test
    public void sumOfEmptyArrayIsZero() {
        assertEquals(0, ArrayUtilities.sum(new int[]{}));
    }


    @Test
    public void canSumSingleElementArray() {
        assertEquals(2, ArrayUtilities.sum(new int[]{2}));
    }


    @Test
    public void canSumMultiElementArray() {
        assertEquals(14, ArrayUtilities.sum(new int[]{2,4,6,2}));
    }

}
