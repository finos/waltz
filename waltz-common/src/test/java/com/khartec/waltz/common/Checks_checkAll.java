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

public class Checks_checkAll {

    @Test(expected = IllegalArgumentException.class)
    public void mustBeGivenAnArray() {
        Checks.checkAll(null, x -> true, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void mustBeGivenAPredicate() {
        Checks.checkAll(new String[]{}, null, "test");
    }


    @Test
    public void passesIfPredicateIsTrueForAll() {
        Checks.checkAll(new Integer[]{1, 3, 5}, x -> (x.intValue() % 2) == 1, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIfPredicateIsFalseForAny() {
        Checks.checkAll(new Integer[]{1, 4, 5}, x -> (x.intValue() % 2) == 1, "test");
    }
}
