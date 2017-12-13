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

import java.util.Map;

import static org.junit.Assert.assertEquals;


public class MapBuilderTest {

    @Test
    public void simple() {

        Map<String, Integer> m =  new MapBuilder<String, Integer>()
                .add("a", 1)
                .build();

        assertEquals(1, m.size());
        assertEquals(Integer.valueOf(1), m.get("a"));

    }


    @Test
    public void differingTypes() {

        Map<String, Object> m =  new MapBuilder<String, Object>()
                .add("a", 1)
                .add("b", "bob")
                .build();

        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(1), m.get("a"));
        assertEquals("bob", m.get("b"));
    }

}
