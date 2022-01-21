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
package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.service.report_grid.ReportGridService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.web.WebUtilities;
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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.ListUtilities.*;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;

@Service
public class ReportGridExtractor implements DataExtractor {

    public static final String BASE_URL = WebUtilities.mkPath("data-extract", "report-grid");
    private final ReportGridService reportGridService;

    @Autowired
    public ReportGridExtractor(ReportGridService reportGridService) {
        this.reportGridService = reportGridService;
    }


    @Override
    public void register() {
        registerGridViewExtract();
    }


    private void registerGridViewExtract() {
        post(WebUtilities.mkPath(BASE_URL, "id", ":id"),
            (request, response) ->
                writeReportResults(
                    response,
                    prepareReport(
                        parseExtractFormat(request),
                        WebUtilities.getId(request),
                        WebUtilities.readIdSelectionOptionsFromBody(request))));
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareReport(ExtractFormat format,
                                                                long gridId,
                                                                IdSelectionOptions selectionOptions) throws IOException {

        ReportGrid reportGrid = reportGridService.getByIdAndSelectionOptions(
                gridId,
                selectionOptions);

        List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows = prepareReportRows(reportGrid);

        return formatReport(
                format,
                mkReportName(reportGrid.definition(), selectionOptions),
                reportGrid.definition().columnDefinitions(),
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
                "Application Id",
                "Application Name",
                "Application Asset Code");

        List<String> columnHeaders = map(
                columnDefinitions,
                r -> r.columnEntityReference().name().get());

        return concat(
                staticHeaders,
                columnHeaders);
    }


    private List<Tuple2<ReportSubject, ArrayList<Object>>> prepareReportRows(ReportGrid reportGrid) {

        Set<ReportGridCell> tableData = reportGrid.instance().cellData();

        Map<Long, ReportSubject> subjectsById = indexBy(d -> d.entityReference().id(), reportGrid.instance().subjects());
        Map<Long, RatingSchemeItem> ratingsById = indexById(reportGrid.instance().ratingSchemeItems());

        Map<Long, Collection<ReportGridCell>> tableDataBySubjectId = groupBy(
                tableData,
                ReportGridCell::subjectId);

        return tableDataBySubjectId
                .entrySet()
                .stream()
                .map(r -> {
                    Long subjectId = r.getKey();

                    ReportSubject subject = subjectsById.getOrDefault(subjectId, null);

                    ArrayList<Object> reportRow = new ArrayList<>();

                    Collection<ReportGridCell> cells = r.getValue();

                    Map<Tuple2<Long, EntityKind>, Object> callValuesByColumnRefForSubject = indexBy(
                            cells,
                            k -> tuple(k.columnEntityId(), k.columnEntityKind()),
                            v -> getValueFromReportRow(ratingsById, v));

                    //find data for columns
                    reportGrid.definition()
                            .columnDefinitions()
                            .forEach(colDef -> reportRow.add(
                                    callValuesByColumnRefForSubject.getOrDefault(
                                            tuple(
                                                    colDef.columnEntityReference().id(),
                                                    colDef.columnEntityReference().kind()),
                                            null)));
                    return tuple(subject, reportRow);
                })
                .sorted(Comparator.comparing(t -> t.v1.entityReference().name().get()))
                .collect(toList());
    }


    private Object getValueFromReportRow(Map<Long, RatingSchemeItem> ratingsById,
                                         ReportGridCell reportGridCell) {
        switch (reportGridCell.columnEntityKind()){
            case COST_KIND:
                return reportGridCell.value();
            case INVOLVEMENT_KIND:
            case SURVEY_QUESTION:
                return Optional.ofNullable(reportGridCell.text()).orElse("-");
            case MEASURABLE:
            case ASSESSMENT_DEFINITION:
                return maybeGet(ratingsById, reportGridCell.ratingId())
                        .map(NameProvider::name)
                        .orElse(null);
            default:
                throw new IllegalArgumentException("This report does not support export with column of type: " + reportGridCell.columnEntityKind().name());
        }
    }


    private Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               String reportName,
                                                               List<ReportGridColumnDefinition> columnDefinitions,
                                                               List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
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
                               List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
        List<String> headers = mkHeaderStrings(columnDefinitions);

        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    private List<Object> simplify(Tuple2<ReportSubject, ArrayList<Object>> row) {

        long appId = row.v1.entityReference().id();
        String appName = row.v1.entityReference().name().get();
        Optional<String> assetCode = row.v1.entityReference().externalId();

        List<Object> appInfo = asList(appId, appName, assetCode);

        return map(concat(appInfo, row.v2), value -> {
            if (value == null) return null;
            if (value instanceof Optional) {
                return ((Optional<?>) value).orElse(null);
            } else {
                return value;
            }
        });
    }


    private byte[] mkExcelReport(String reportName,
                                 List<ReportGridColumnDefinition> columnDefinitions,
                                 List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(ExtractorUtilities.sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(columnDefinitions, sheet);
        writeExcelBody(reportRows, sheet);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colCount - 1));
        sheet.createFreezePane(0, 1);

        return convertExcelToByteArray(workbook);
    }


    private byte[] convertExcelToByteArray(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
    }


    private int writeExcelBody(List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows, XSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(1);
        reportRows.forEach(r -> {

            long subjectId = r.v1.entityReference().id();
            String subjectName = r.v1.entityReference().name().get();
            Optional<String> subjectExtId = r.v1.entityReference().externalId();
            LifecyclePhase subjectLifecyclePhase = r.v1.lifecyclePhase();

            List<Object> subjectInfo = asList(subjectId, subjectName, subjectExtId, subjectLifecyclePhase);

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
