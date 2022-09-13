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
package org.finos.waltz.web.endpoints.extracts.reportgrid;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.report_grid.ReportGrid;
import org.finos.waltz.model.report_grid.ReportGridFixedColumnDefinition;
import org.finos.waltz.model.report_grid.ReportSubject;
import org.finos.waltz.web.endpoints.extracts.ColumnCommentary;
import org.finos.waltz.web.endpoints.extracts.ExtractorUtilities;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.StringUtilities.length;
import static org.finos.waltz.common.StringUtilities.limit;


@Component
public class DynamicExcelFormatter implements DynamicFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicExcelFormatter.class);
    private final FormatterUtils formatterUtils;
    private final String CELL_LIMIT_MESSAGE = "...Data truncated, excel cell limit reached. Export using CSV for complete data.";



    public DynamicExcelFormatter(FormatterUtils formatterUtils){
        this.formatterUtils = formatterUtils;
    }


    @Override
    public byte[] format(String id,
                         ReportGrid reportGrid,
                         List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions,
                         List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows)  throws IOException{
        try {
            LOG.info("Generating Excel report {}",id);
            return mkExcelReport(id,columnDefinitions,reportRows);
        } catch (IOException e) {
           LOG.warn("Encounter error when trying to generate CSV report.  Details:{}", e.getMessage());
           throw e;
        }
    }


    private byte[] mkExcelReport(String reportName,
                                 List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions,
                                 List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = workbook.createSheet(ExtractorUtilities.sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(columnDefinitions, sheet);
        writeExcelBody(reportRows, sheet);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colCount - 1));
        sheet.createFreezePane(0, 1);

        return convertExcelToByteArray(workbook);
    }


    private byte[] convertExcelToByteArray(SXSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
    }


    private int writeExcelBody(List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows, SXSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(1);
        int maxCellLength = 32767 - length(CELL_LIMIT_MESSAGE);
        reportRows.forEach(r -> {

            long subjectId = r.v1.entityReference().id();
            String subjectName = r.v1.entityReference().name().get();
            Optional<String> externalId = r.v1.entityReference().externalId();
            LifecyclePhase lifecyclePhase = r.v1.lifecyclePhase();

            List<Object> subjectInfo = asList(subjectId, subjectName, externalId, lifecyclePhase.name());

            List<Object> values = concat(subjectInfo, r.v2);

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
                } else if (v instanceof ExternalIdValue) {
                    Cell cell = row.createCell(nextColNum, CellType.STRING);
                    cell.setCellValue(((ExternalIdValue) v).value());
                } else {
                    Cell cell = row.createCell(nextColNum);
                    String cellValue = Objects.toString(v);
                    String cellValueToWrite = (length(cellValue) >= maxCellLength)
                            ? String.format("%s%s", limit(cellValue, maxCellLength), CELL_LIMIT_MESSAGE)
                            : cellValue;
                    cell.setCellValue(cellValueToWrite);
                }

            }
        });
        return rowNum.get();
    }


    private int writeExcelHeader(List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions, SXSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();
        formatterUtils.mkHeaderStrings(columnDefinitions).forEach(hdr -> writeExcelHeaderCell(headerRow, colNum, hdr));
        return colNum.get();
    }


    private void writeExcelHeaderCell(Row headerRow, AtomicInteger colNum, String text) {
        Cell cell = headerRow.createCell(colNum.getAndIncrement());
        cell.setCellValue(text);
    }


}
