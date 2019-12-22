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
