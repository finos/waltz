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

package com.khartec.waltz.model.physical_specification;

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.EnumUtilities;

import java.util.function.Function;

/**
 * How an specification manifests itself 'physically'.
 * <p/>
 * The UNKNOWN option is not intended to be exposed as
 * as selectable choice for users.  It is intended to be
 * used when bulk importing from systems which do not have
 * any equivalent format representation
 */
public enum DataFormatKind {

    BINARY,
    DATABASE,
    FLAT_FILE,
    JSON,
    OTHER,
    UNSTRUCTURED,
    UNKNOWN,
    XML;


    private static final Aliases<DataFormatKind> defaultAliases = new Aliases<>()
            .register(DataFormatKind.FLAT_FILE,
                    "CSV_FILE", "CSV", "EXCEL", "PDF", "EXCEL/PDF",
                    "FILE", "TEXT", "XL", "XLS", "XLM", "PIPE DELIMITED")
            .register(DataFormatKind.DATABASE, "DATA")
            .register(DataFormatKind.UNSTRUCTURED, "EMAIL", "STRING", "TXT")
            .register(DataFormatKind.OTHER, "OTHER", "JOB_ROW_CRM")
            .register(DataFormatKind.XML, "Webservice");


    public static DataFormatKind parse(String value, Function<String, DataFormatKind> failedParseSupplier) {
        return EnumUtilities.parseEnumWithAliases(value, DataFormatKind.class, failedParseSupplier, defaultAliases);
    }
}
