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
