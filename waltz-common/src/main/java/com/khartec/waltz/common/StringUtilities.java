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

import java.util.Optional;


public class StringUtilities {

    /**
     * Determines if the given string is null or empty (after trimming).
     * <code>notEmpty</code> provides the inverse.
     * @param x the String to check
     * @return true iff x is null or x.trim is empty
     */
    public static boolean isEmpty(String x) {
        return x == null || x.trim().equals("");
    }

    /**
     * Convenience method for <code> ! isEmpty(x) </code>
     * @param x the String to check
     * @return true iff x.trim is non empty
     */
    public static boolean notEmpty(String x) {
        return ! isEmpty(x);
    }


    public static boolean isEmpty(Optional<String> maybeString) {
        if (maybeString == null) return true;
        if (! maybeString.isPresent()) return true;
        return isEmpty(maybeString.get());
    }

    public static Long parseLong(String value, Long dflt) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            return dflt;
        }
    }
}
