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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class IOUtilities_readLines {

    @Test
    public void canReadLines() throws IOException {
        List<String> lines = IOUtilities.readLines(getStream("lines.txt"));
        assertEquals(4, lines.size());
    }


    @Test
    public void emptyGivesEmptyList() throws IOException {
        List<String> lines = IOUtilities.readLines(getStream("empty.txt"));
        assertEquals(0, lines.size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void nullStreamThrowsException() throws IOException {
        IOUtilities.readLines(null);
    }


    private InputStream getStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

}
