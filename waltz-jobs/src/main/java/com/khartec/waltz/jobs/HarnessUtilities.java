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

package com.khartec.waltz.jobs;

import java.util.function.Supplier;

public class HarnessUtilities {

    public static <T> T time(String name, Supplier<T> s) {
        System.out.println("-- begin [" + name + "]");

        long st = System.currentTimeMillis();

        try {
            T r = s.get();
            long end = System.currentTimeMillis();

            System.out.println("-- end [" + name + "]");
            System.out.println("-- dur [" + name + "]:" + (end - st));
            System.out.println("-- result [" + name + "]:" + r);
            System.out.println();

            return r;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }
}
