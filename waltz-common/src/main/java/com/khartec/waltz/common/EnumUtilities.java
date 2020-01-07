/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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


    /**
     * Given an array of enums (typically via <code>MyEnum.values()</code>)
     * this will return a {@link Set} of the enum names
     * @param enums Array of enums to get the names from
     * @param <T> Type of enum values
     * @return Set of names associated to the given enum
     */
    public static <T extends Enum> Set<String> names(T... enums) {
        return Stream
                .of(enums)
                .map(t -> t.name())
                .collect(Collectors.toSet());
    }


    public static <T extends Enum> Set<String> names(Collection<T> enums) {
        return enums
                .stream()
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
