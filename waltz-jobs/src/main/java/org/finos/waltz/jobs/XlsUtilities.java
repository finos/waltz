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

package org.finos.waltz.jobs;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.function.Function;
import java.util.stream.Stream;

public class XlsUtilities {

    public static <T> T mapStrCell(Row row, int i, Function<String, T> mapper) {
        return mapper.apply(strVal(row, i));
    }


    public static String strVal(Row row, int offset) {
        Cell cell = row.getCell(offset);
        return cell == null ? null : cell.getStringCellValue();
    }


    public static Stream<Row> streamRows(Workbook workbook, SheetNumProvider sheetDefinition) {
        return streamRows(workbook.getSheetAt(sheetDefinition.sheetNum()));
    }


    public static Stream<Row> streamRows(Sheet sheet) {
        Stream.Builder<Row> streamBuilder = Stream.builder();
        sheet.rowIterator()
                .forEachRemaining(streamBuilder::add);
        return streamBuilder.build();
    }

}
