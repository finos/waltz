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


public class CollectionUtilities {

    public static <T> Optional<T> find(Predicate<T> pred, Collection<T> ts) {
        return ts.stream()
                .filter(pred)
                .findFirst();
    }


    public static <T> T first(Collection<T> ts) {
        Checks.checkNotNull(ts, "list must not be null");
        Checks.checkFalse(ts.isEmpty(), "list cannot be empty");

        return ts.iterator().next();
    }
}
