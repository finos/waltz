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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static com.khartec.waltz.common.ArrayUtilities.all;

public class Checks {

    /**
     * @param t Value to check
     * @param message Text to use in the exception message
     * @param <T> Type of the value
     * @return The given value, facilitates method chaining
     * @throw IllegalArgumentException If the value <code>t</code> is null
     */
    public static <T> T checkNotNull(T t, String message) {
        checkTrue(t != null, message);
        return t;
    }

    /**
     * Verifies all elements of an array comply to a given predicate.
     * @param ts Array of elements
     * @param check Predicate function used to check each element
     * @param message Text to use in any exception message
     * @param <T> Type of the elements
     * @return The given array
     * @throws IllegalArgumentException If any of the elements fail the predicate
     */
    public static <T> T[] checkAll(T[] ts, Predicate<T> check, String message) {
        checkNotNull(ts, message + ": Array was null");
        checkNotNull(check, message + ": Predicate was null");

        checkTrue(all(ts, check), message);
        return ts;
    }

    /**
     * Verifies that the boolean <code>b</code> is true
     * @param b Boolean to check
     * @param msg Text to use in any exception method
     * @throws IllegalArgumentException if <code>b != true</code>
     */
    public static void checkTrue(boolean b, String msg) {
        if (! b) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void checkFalse(boolean b, String message) {
        checkTrue(! b, message);
    }

    public static String checkNotEmptyString(String str, String message) {
        checkNotNull(str, message);
        checkFalse(str.trim().equals(""), message);
        return str;
    }


    public static <T> T checkOptionalIsPresent(Optional<T> optional, String message) {
        return optional
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    public static <T> void checkNotEmpty(Collection<T> ts, String message) {
        checkNotNull(ts, message);
        checkFalse(ts.isEmpty(), message);
    }
}
