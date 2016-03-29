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

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EnumUtilities {

    public static <T extends Enum<T>> T readEnum(String value, Class<T> enumClass, T dflt) {
        Checks.checkNotNull(enumClass, "Enum class must be supplied");
        EnumSet<T> set = EnumSet.allOf(enumClass);
        for (T t : set) {
            if (t.name().equals(value)) {
                return t;
            }
        }
        return dflt;
    }


    public static <T extends Enum> Set<String> names(T... enums) {
        return Stream
                .of(enums)
                .map(t -> t.name())
                .collect(Collectors.toSet());
    }
}
