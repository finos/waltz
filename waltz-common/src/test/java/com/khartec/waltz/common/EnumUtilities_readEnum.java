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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.khartec.waltz.common.EnumUtilities.readEnum;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnumUtilities_readEnum {

    private enum MyEnum {
        A,
        B
    }


    @Test
    public void canReadEnum() {
        assertEquals(MyEnum.A, readEnum("A", MyEnum.class, (s) -> null));
        assertEquals(MyEnum.B, readEnum("B", MyEnum.class, (s) -> null));
    }


    @Test
    public void defaultValueUsedIfNoMatch() {
        assertEquals(MyEnum.A, readEnum("Z", MyEnum.class, (s) -> MyEnum.A));
        assertEquals(null, readEnum("Z", MyEnum.class, (s) -> null));
    }


    @Test(expected = IllegalArgumentException.class)
    public void badIfNoEnumClass() {
        readEnum("A", null, (s) -> MyEnum.A);
    }

}
