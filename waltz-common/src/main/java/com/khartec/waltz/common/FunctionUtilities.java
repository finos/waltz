/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.common;

import java.util.function.Supplier;

/**
 * Created by dwatkins on 26/12/2015.
 */
public class FunctionUtilities {


    /**
     * This odd looking function can be used as a wrapper
     * to explicitly discard a result of another function,
     * effectively turning the wrapped functions return type
     * into void.
     * @param x - the parameter to discard
     */
    public static void discardResult(Object x) {
    }

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
