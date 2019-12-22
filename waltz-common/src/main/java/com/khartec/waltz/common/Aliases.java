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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;


public class Aliases<T> {

    private final HashMap<String, T> mappings = new HashMap<>();

    public Aliases register(T val, String... aliases) {
        mappings.put(sanitise(val.toString()), val);
        Arrays.stream(aliases)
                .map(this::sanitise)
                .forEach(s -> mappings.put(s, val));

        return this;
    }


    public Optional<T> lookup(String alias) {
        return Optional.ofNullable(mappings.get(sanitise(alias)));
    }


    private String sanitise(String s) {
        final char[] fillerChars = new char[]{'-', '_', '(', ')', '{', '}', '/', '\\', ',', '.'};

        String valueNormalised = s.trim();

        for (char c : fillerChars) {
            valueNormalised = valueNormalised.replace(c, ' ').replaceAll(" ", "");
        }

        return valueNormalised
                .toLowerCase()
                .trim();
    }

}
