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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;


public class OptionalUtilities {

    /**
     * Synonym for ofNullable as it reads better.
     * @param value
     * @return
     */
    public static <T> Optional<T> maybe(T value) {
        return Optional.ofNullable(value);
    }


    /**
     * Given a list of optional values will return
     * a list containing only the values that are not
     * empty (unpacked)
     * @param optionals
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(Optional<T>... optionals) {
        if (optionals == null) { return Collections.emptyList(); }

        return Stream
                .of(optionals)
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());

    }


    /**
     * Returns true iff, `opt` is empty and val `is` null, or, `opt.get` equals `val`
     */
    public static <T> boolean contentsEqual(Optional<T> opt, T val) {
        checkNotNull(opt, "'opt' cannot be null");
        return opt
                .map(v -> v.equals(val))
                .orElse(val == null);
    }


    /**
     * Takes an optional value (that may itself be null) and makes it a non-null optional
     * @param nullable  an Optional value that may, itself, be null
     * @param <T>  type of the optional value
     * @return  an Optional
     */
    public static <T> Optional<T> ofNullableOptional(Optional<T> nullable) {
        return nullable == null
                ? Optional.empty()
                : nullable;
    }

}
