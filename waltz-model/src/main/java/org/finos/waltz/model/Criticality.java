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

package org.finos.waltz.model;

import org.finos.waltz.common.Aliases;
import org.finos.waltz.common.EnumUtilities;

import java.util.function.Function;


/**
 * Should use CriticalityValue instead
 */
@Deprecated
public enum Criticality {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH,
    NONE,
    UNKNOWN;


    private static final Aliases<Criticality> defaultAliases = new Aliases<>();


    public static Criticality parse(String value, Function<String, Criticality> failedParseSupplier) {
        return EnumUtilities.parseEnumWithAliases(value, Criticality.class, failedParseSupplier, defaultAliases);
    }
}
