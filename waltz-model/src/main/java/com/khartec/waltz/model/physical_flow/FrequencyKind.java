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

package com.khartec.waltz.model.physical_flow;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumUtilities;

import java.util.function.Function;

/**
 * Represents the frequency a data flow is 'activated'
 * <p/>
 * The UNKNOWN option is not intended to be exposed as
 * as selectable choice for users.  It is intended to be
 * used when bulk importing from systems which do not have
 * any equivalent frequency representation
 */
public enum FrequencyKind {
    ON_DEMAND, // pull
    REAL_TIME, // push
    INTRA_DAY,
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    BIANNUALLY,
    YEARLY,
    UNKNOWN;


    private static final Aliases<FrequencyKind> defaultAliases = new Aliases<>()
            .register(FrequencyKind.YEARLY, "ANNUALLY")
            .register(FrequencyKind.BIANNUALLY, "BIANNUAL", "BIANUALY")
            .register(FrequencyKind.DAILY, "DAILY-WEEKDAYS")
            .register(FrequencyKind.INTRA_DAY, "INTRADAY")
            .register(FrequencyKind.ON_DEMAND, "PER REQUEST", "REQUEST BASIS", "AD HOC", "ON REQUEST")
            .register(FrequencyKind.MONTHLY, "MONTHLY")
            .register(FrequencyKind.QUARTERLY, "QUARTERLY")
            .register(FrequencyKind.WEEKLY)
            .register(FrequencyKind.REAL_TIME, "REAL", "REALTIME", "REAL-TIME");


    public static FrequencyKind parse(String value, Function<String, FrequencyKind> failedParseSupplier) {
        return EnumUtilities.parseEnumWithAliases(value, FrequencyKind.class, failedParseSupplier, defaultAliases);
    }
}
