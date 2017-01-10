/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;


public class EnumUtilities {

    public static <T extends Enum<T>> T readEnum(String value, Class<T> enumClass, Function<String, T> failedParseSupplier) {
        checkNotNull(enumClass, "Enum class must be supplied");
        checkNotNull(failedParseSupplier, "failedParseSupplier cannot be null");

        EnumSet<T> set = EnumSet.allOf(enumClass);
        for (T t : set) {
            if (t.name().equals(value)) {
                return t;
            }
        }
        return failedParseSupplier.apply(value);
    }


    public static <T extends Enum> Set<String> names(T... enums) {
        return Stream
                .of(enums)
                .map(t -> t.name())
                .collect(Collectors.toSet());
    }
}
