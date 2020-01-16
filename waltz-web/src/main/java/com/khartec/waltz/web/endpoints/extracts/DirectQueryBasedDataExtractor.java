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


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jetty.http.MimeTypes;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Select;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.Optional.ofNullable;


public abstract class DirectQueryBasedDataExtractor implements DataExtractor {

    protected DSLContext dsl;


    public DirectQueryBasedDataExtractor(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    protected Object writeExtract(String suggestedFilenameStem,
                                  Select<?> qry,
                                  Request request,
                                  Response response) throws IOException {
        ExtractFormat format = parseExtractFormat(request);
        switch (format) {
            case XLSX:
                return writeAsExcel(suggestedFilenameStem, qry, response);
            case CSV:
                return writeAsCSV(suggestedFilenameStem, qry, response);
            default:
                throw new IllegalArgumentException("Cannot write extract using unknown format: " + format);
        }
    }


    private Object writeAsExcel(String suggestedFilenameStem,
                                Select<?> qry,
                                Response response) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sanitizeSheetName(suggestedFilenameStem));

        writeExcelHeader(qry, sheet);
        writeExcelBody(qry, sheet);

        int endFilterColumnIndex = qry.fields().length == 0
                ? 0
                : qry.fields().length - 1;

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, endFilterColumnIndex));
        sheet.createFreezePane(0, 1);

        byte[] bytes = convertExcelToByteArray(workbook);

        HttpServletResponse httpResponse = response.raw();

        httpResponse.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        httpResponse.setHeader("Content-Disposition", "attachment; filename=" + suggestedFilenameStem + ".xlsx");
        httpResponse.setHeader("Content-Transfer-Encoding", "7bit");

        httpResponse.setContentLength(bytes.length);
        httpResponse.getOutputStream().write(bytes);
        httpResponse.getOutputStream().flush();
        httpResponse.getOutputStream().close();

        return httpResponse;
    }


    private Object writeAsCSV(String suggestedFilenameStem,
                              Select<?> qry,
                              Response response) {
        String csv = qry.fetch().formatCSV();
        response.type(MimeTypes.Type.TEXT_PLAIN.name());
        response.header("Content-disposition", "attachment; filename=" + suggestedFilenameStem + ".csv");
        return csv;
    }


    private byte[] convertExcelToByteArray(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
    }


    private void writeExcelBody(Select<?> qry, XSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(1);
        qry.fetch().forEach(r -> {
            Row row = sheet.createRow(rowNum.getAndIncrement());
            AtomicInteger colNum = new AtomicInteger(0);
            for (Field<?> field : r.fields()) {
                Cell cell = row.createCell(colNum.getAndIncrement());
                ofNullable(r.get(field)).ifPresent(v -> {
                    if (v instanceof Number) {
                        cell.setCellType(CellType.NUMERIC);
                        cell.setCellValue(((Number) v).doubleValue());
                    } else {
                        cell.setCellValue(Objects.toString(v));
                    }
                });
            }
        });
    }


    private void writeExcelHeader(Select<?> qry, XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();
        qry.fieldStream().forEach(f -> {
            Cell cell = headerRow.createCell(colNum.getAndIncrement());
            cell.setCellValue(Objects.toString(f.getName()));
        });
    }

}
