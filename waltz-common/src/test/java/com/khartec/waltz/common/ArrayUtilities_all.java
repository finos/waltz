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

import static com.khartec.waltz.common.ArrayUtilities.all;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArrayUtilities_all {

    @Test(expected = IllegalArgumentException.class)
    public void arrayMustNotBeNull() {
        all(null, d -> true);
    }


    @Test(expected = IllegalArgumentException.class)
    public void predicateMustNotBeNull() {
        all(new Object[] {}, null);
    }


    @Test
    public void allOverAnEmptyArrayIsTrue() {
        assertTrue(all(new Object[]{}, d -> true));
    }


    @Test
    public void failingPredicateFailsAll() {
        assertTrue(all(new Integer[] {1}, d -> d == 1));
        assertFalse(all(new Integer[] {1, 2}, d -> d == 1));
    }

}
