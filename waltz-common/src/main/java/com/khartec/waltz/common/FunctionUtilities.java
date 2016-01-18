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

import java.util.function.Function;

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

}
