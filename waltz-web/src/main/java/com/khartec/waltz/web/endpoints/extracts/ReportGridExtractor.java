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

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.report_grid.ReportGridDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.report_grid.ReportGridColumnDefinition;
import com.khartec.waltz.model.report_grid.ReportGridDefinition;
import com.khartec.waltz.model.report_grid.ReportGridRatingCell;
import com.khartec.waltz.schema.tables.records.ApplicationRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.tuple.Tuple2;
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
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.schema.Tables.APPLICATION;
import static com.khartec.waltz.schema.Tables.RATING_SCHEME_ITEM;
import static com.khartec.waltz.web.WebUtilities.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;

@Service
public class ReportGridExtractor implements DataExtractor {

    public static final String BASE_URL = mkPath("data-extract", "report-grid");
    private final DSLContext dsl;
    private final ReportGridDao reportGridDao;

    @Autowired
    public ReportGridExtractor(DSLContext dsl,
                               ReportGridDao reportGridDao) {
        this.dsl = dsl;
        this.reportGridDao = reportGridDao;
    }


    @Override
    public void register() {
        registerGridViewExtract();
    }

    private void registerGridViewExtract() {
        post(mkPath(BASE_URL, "id", ":id"),
            (request, response) ->
                writeReportResults(
                    response,
                    prepareReport(
                        parseExtractFormat(request),
                        getId(request),
                        readIdSelectionOptionsFromBody(request))));
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareReport(ExtractFormat format,
                                                                long gridId,
                                                                IdSelectionOptions selectionOptions) throws IOException {

        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
        Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(selectionOptions);

        ReportGridDefinition gridDefinition = reportGridDao.getGridDefinitionById(gridId);
        Set<ReportGridRatingCell> tableData = reportGridDao.findCellDataByGridId(gridId, appIdSelector);

        List<List<Object>> reportRows = prepareReportRows(tableData, gridDefinition.columnDefinitions());

        return formatReport(
                format,
                mkReportName(gridDefinition, selectionOptions),
                gridDefinition.columnDefinitions(),
                reportRows);
    }


    private String mkReportName(ReportGridDefinition gridDefinition, IdSelectionOptions selectionOptions) {

        return format("%s_%s_%s",
                gridDefinition.name(),
                selectionOptions.entityReference().kind(),
                selectionOptions.entityReference().id());
    }


    private List<String> mkHeaderStrings(List<ReportGridColumnDefinition> columnDefinitions) {
        List<String> staticHeaders = newArrayList(
                "APPLICATION ID",
                "APPLICATION NAME",
                "APPLICATION ASSET CODE");

        List<String> columnHeaders = map(columnDefinitions,
                r -> r.columnEntityReference().name().get());

        return concat(
                staticHeaders,
                columnHeaders);
    }


    private List<List<Object>> prepareReportRows(Set<ReportGridRatingCell> tableData, List<ReportGridColumnDefinition> columnDefinitions) {

        Map<Long, Collection<ReportGridRatingCell>> tableDataByAppId = groupBy(tableData, ReportGridRatingCell::applicationId);

        Map<Long, String> ratingSchemeIdToName = dsl
                .select(RATING_SCHEME_ITEM.ID, RATING_SCHEME_ITEM.NAME)
                .from(RATING_SCHEME_ITEM)
                .where(RATING_SCHEME_ITEM.ID.in(map(tableData, ReportGridRatingCell::ratingId)))
                .fetchMap(RATING_SCHEME_ITEM.ID, RATING_SCHEME_ITEM.NAME);

        Map<Long, ApplicationRecord> applicationsById = dsl
                .selectFrom(APPLICATION)
                .where(APPLICATION.ID.in(tableDataByAppId.keySet()))
                .fetchMap(APPLICATION.ID);

        return tableDataByAppId
                .entrySet()
                .stream()
                .map(r -> {
                    ArrayList<Object> reportRow = new ArrayList<>();

                    Long appId = r.getKey();
                    ApplicationRecord app = applicationsById.getOrDefault(appId, null);

                    //start with app data
                    reportRow.add(appId);
                    reportRow.add(app.get(APPLICATION.NAME));
                    reportRow.add(app.get(APPLICATION.ASSET_CODE));

                    Map<Tuple2<Long, EntityKind>, String> ratingsByColumnRefForApp = indexBy(r.getValue(),
                            d -> tuple(d.columnEntityId(), d.columnEntityKind()),
                            v -> ratingSchemeIdToName.getOrDefault(v.ratingId(), null));

                    //find data for columns
                    columnDefinitions
                            .stream()
                            .forEach(t -> {
                                reportRow.add(ratingsByColumnRefForApp.getOrDefault(
                                        tuple(t.columnEntityReference().id(), t.columnEntityReference().kind()),
                                        null));
                            });
                    return reportRow;
                })
                .sorted((r1, r2) -> {

                    String app1Name = String.valueOf(r1.get(1));
                    String app2Name = String.valueOf(r2.get(1));

                    return app1Name.compareTo(app2Name);
                })
                .collect(toList());
    }


    private Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               String reportName,
                                                               List<ReportGridColumnDefinition> columnDefinitions,
                                                               List<List<Object>> reportRows) throws IOException {
        switch (format) {
            case XLSX:
                return tuple(format, reportName, mkExcelReport(reportName, columnDefinitions, reportRows));
            case CSV:
                return tuple(format, reportName, mkCSVReport(columnDefinitions, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private byte[] mkCSVReport(List<ReportGridColumnDefinition> columnDefinitions,
                               List<List<Object>> reportRows) throws IOException {
        List<String> headers = mkHeaderStrings(columnDefinitions);

        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
        csvWriter.flush();

        return writer.toString().getBytes();
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


    private byte[] mkExcelReport(String reportName, List<ReportGridColumnDefinition> columnDefinitions, List<List<Object>> reportRows) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(columnDefinitions, sheet);
        writeExcelBody(reportRows, sheet);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colCount));
        sheet.createFreezePane(0, 1);

        return convertExcelToByteArray(workbook);
    }


    private byte[] convertExcelToByteArray(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
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


    private int writeExcelHeader(List<ReportGridColumnDefinition> columnDefinitions, XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();

        mkHeaderStrings(columnDefinitions).forEach(hdr -> writeExcelHeaderCell(headerRow, colNum, hdr));

        return colNum.get();
    }


    private void writeExcelHeaderCell(Row headerRow, AtomicInteger colNum, String text) {
        Cell cell = headerRow.createCell(colNum.getAndIncrement());
        cell.setCellValue(text);
    }

}
