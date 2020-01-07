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

package com.khartec.waltz.jobs;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.function.Function;
import java.util.stream.Stream;

public class XlsUtilities {

    public static <T> T mapStrCell(Row row, int i, Function<String, T> mapper) {
        return mapper.apply(strVal(row, i));
    }


    public static String strVal(Row row, int offset) {
        return row.getCell(offset).getStringCellValue();
    }


    public static Stream<Row> streamRows(Workbook workbook, SheetNumProvider sheetDefinition) {
        Stream.Builder<Row> streamBuilder = Stream.builder();
        workbook.getSheetAt(sheetDefinition.sheetNum())
                .rowIterator()
                .forEachRemaining(r -> streamBuilder.add(r));
        return streamBuilder.build();
    }

}
