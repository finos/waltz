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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.tokenise;


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


    public static <T extends Enum<T>> T parseEnumWithAliases(String value,
                                                             Class<T> enumClass,
                                                             Function<String, T> failedParseSupplier,
                                                             Aliases<T> aliases) {
        checkNotEmpty(value, "value cannot be empty");

        String valueNormalised = value.trim()
                .toUpperCase()
                .replace(' ', '_')
                .replace('-', '_');

        T parsed = readEnum(valueNormalised, enumClass, (s) -> null);

        if(parsed != null) return parsed;

        // now doing it with fallback
        T fallbackEnum = fallbackParseEnum(value, aliases);
        return fallbackEnum != null ? fallbackEnum : failedParseSupplier.apply(value);
    }


    public static <T extends Enum> Set<String> names(T... enums) {
        return Stream
                .of(enums)
                .map(t -> t.name())
                .collect(Collectors.toSet());
    }

    public static <T extends Enum> Set<String> names(Collection<T> enums) {
        return enums.stream()
                .map(t -> t.name())
                .collect(Collectors.toSet());
    }


    private static <T extends Enum<T>> T fallbackParseEnum(String value, Aliases<T> aliases) {
        checkNotNull(value, "value cannot be null");

        String normalisedValue = value.trim().toUpperCase();
        Optional<T> lookup = aliases.lookup(normalisedValue);
        if (lookup.isPresent()) {
            return lookup.get();
        }

        List<String> tokens = tokenise(normalisedValue);
        for (String token : tokens) {
            Optional<T> tokenLookup = aliases.lookup(token);
            if (tokenLookup.isPresent()) {
                return tokenLookup.get();
            }
        }
        return null;
    }
}
