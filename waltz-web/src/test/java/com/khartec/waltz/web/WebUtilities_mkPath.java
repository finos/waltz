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

package com.khartec.waltz.web;

import org.junit.Test;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static org.junit.Assert.assertEquals;

public class WebUtilities_mkPath {

    @Test
    public void oneSegment() {
        assertEquals("bob", mkPath("bob"));
    }

    @Test
    public void zeroSegments() {
        assertEquals("", mkPath());
    }

    @Test
    public void multipleSegments() {
        assertEquals("ay/bee/cee", mkPath("ay", "bee", "cee"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptySegmentsThrowException() {
        mkPath("ay", "", "cee");
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotMkPathIfItContainsNullSegments() {
        mkPath("bob", null, "jones");
    }

}
