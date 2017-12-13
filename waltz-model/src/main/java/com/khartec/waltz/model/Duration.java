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

package com.khartec.waltz.model;

public enum Duration {

    DAY(1),
    WEEK(7),
    MONTH(31),
    QUARTER(MONTH.numDays * 3),
    HALF_YEAR(MONTH.numDays * 6),
    YEAR(MONTH.numDays * 12),
    ALL(Integer.MAX_VALUE);


    private final int numDays;


    Duration(int numDays) {
        this.numDays = numDays;
    }


    public int numDays() {
        return numDays;
    }
}
