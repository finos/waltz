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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.PhysicalFlow;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.khartec.waltz.common.ListUtilities.*;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.*;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.web.WebUtilities.*;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;


@Service
public abstract class CustomDataExtractor implements DataExtractor {

    public Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               String reportName,
                                                               List<List<Object>> reportRows,
                                                               List<String> headers) throws IOException {
        switch (format) {
            case XLSX:
                return tuple(format, reportName, mkExcelReport(reportName, reportRows, headers));
            case CSV:
                return tuple(format, reportName, mkCSVReport(reportRows, headers));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }

    private byte[] mkCSVReport(List<List<Object>> reportRows, List<String> headers) throws IOException {
        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    private byte[] mkExcelReport(String reportName, List<List<Object>> reportRows, List<String> headers) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(sheet, headers);
        writeExcelBody(reportRows, sheet);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colCount));
        sheet.createFreezePane(0, 1);

        return convertExcelToByteArray(workbook);
    }


    private int writeExcelHeader(XSSFSheet sheet, List<String> headers) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();

        headers.forEach(hdr -> writeExcelHeaderCell(headerRow, colNum, hdr));

        return colNum.get();
    }

    private void writeExcelHeaderCell(Row headerRow, AtomicInteger colNum, String text) {
        Cell cell = headerRow.createCell(colNum.getAndIncrement());
        cell.setCellValue(text);
    }

    private int writeExcelBody(List<List<Object>> reportRows, XSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(1);
        reportRows.forEach(values -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            AtomicInteger colNum = new AtomicInteger(0);
            for (Object value : values) {
                Object v = value instanceof Optional
                        ? ((Optional<?>) value).orElse(null)
                        : value;

                int nextColNum = colNum.getAndIncrement();

                if (v == null) {
                    row.createCell(nextColNum);
                } else if (v instanceof Number) {
                    Cell cell = row.createCell(nextColNum, CellType.NUMERIC);
                    cell.setCellValue(((Number) v).doubleValue());
                } else {
                    Cell cell = row.createCell(nextColNum);
                    cell.setCellValue(Objects.toString(v));
                }

            }
        });
        return rowNum.get();
    }

    private byte[] convertExcelToByteArray(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
    }

    private List<Object> simplify(List<Object> row) {
        return map(row, value -> {
            if (value == null) return null;
            if (value instanceof Optional) {
                return ((Optional<?>) value).orElse(null);
            } else {
                return value;
            }
        });
    }
}
