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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.utils.IdUtilities;
import org.finos.waltz.service.report_grid.ReportGridService;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.survey.SurveyQuestionService;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.ListUtilities.*;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.utils.IdUtilities.getIdOrDefault;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;

@Service
public class ReportGridExtractor implements DataExtractor {

    public static final String BASE_URL = WebUtilities.mkPath("data-extract", "report-grid");
    private final ReportGridService reportGridService;
    private final SurveyQuestionService surveyQuestionService;
    private final SettingsService settingsService;

    @Autowired
    public ReportGridExtractor(ReportGridService reportGridService,
                               SurveyQuestionService surveyQuestionService,
                               SettingsService settingsService) {

        this.reportGridService = reportGridService;
        this.surveyQuestionService = surveyQuestionService;
        this.settingsService = settingsService;
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

        Set<Tuple2<ReportGridColumnDefinition, Boolean>> colsWithCommentRequirement = enrichColsWithCommentRequirement(reportGrid);

        List<Tuple2<Application, ArrayList<Object>>> reportRows = prepareReportRows(colsWithCommentRequirement, reportGrid.instance());

        return formatReport(
                format,
                mkReportName(reportGrid.definition(), selectionOptions),
                colsWithCommentRequirement,
                reportRows);
    }


    private Set<Tuple2<ReportGridColumnDefinition, Boolean>> enrichColsWithCommentRequirement(ReportGrid reportGrid) {
        Set<Long> surveyQuestionsIds = reportGrid
                .definition()
                .columnDefinitions()
                .stream()
                .filter(r -> r.columnEntityKind() == EntityKind.SURVEY_QUESTION)
                .map(ReportGridColumnDefinition::columnEntityId)
                .collect(toSet());

        Set<Long> colsNeedingComments = surveyQuestionService
                .findForIds(surveyQuestionsIds)
                .stream()
                .filter(SurveyQuestion::allowComment)
                .map(q -> q.id().get())
                .collect(toSet());

        return reportGrid
                .definition()
                .columnDefinitions()
                .stream()
                .map(cd -> tuple(cd, cd.columnEntityKind().equals(EntityKind.SURVEY_QUESTION) && colsNeedingComments.contains(cd.columnEntityId())))
                .collect(toSet());
    }


    private String mkReportName(ReportGridDefinition gridDefinition, IdSelectionOptions selectionOptions) {
        return format("%s_%s_%s",
                gridDefinition.name(),
                selectionOptions.entityReference().kind(),
                selectionOptions.entityReference().id());
    }


    private List<String> mkHeaderStrings(Set<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions) {
        List<String> staticHeaders = newArrayList(
                "Application Id",
                "Application Name",
                "Application Asset Code",
                "Application Lifecycle Phase");

        List<String> columnHeaders = columnDefinitions
                .stream()
                .flatMap(r -> {
                    String name = getColumnName(r.v1);
                    Boolean needsComment = r.v2;
                    return Stream.of(
                            name,
                            needsComment
                                ? String.format("%s: comment", name)
                                    : null);

                })
                .filter(Objects::nonNull)
                .collect(toList());

        return concat(
                staticHeaders,
                columnHeaders);
    }

    private String getColumnName(ReportGridColumnDefinition column) {
        if (column.displayName() != null) {
            return column.displayName();
        } else {
            String fieldName = Optional
                    .ofNullable(column.entityFieldReference())
                    .map(EntityFieldReference::displayName)
                    .orElse(null);

            return ListUtilities.asList(fieldName, column.columnName())
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" / "));
        }
    }


    private List<Tuple2<Application, ArrayList<Object>>> prepareReportRows(Set<Tuple2<ReportGridColumnDefinition, Boolean>> colsWithCommentRequirement,
                                                                           ReportGridInstance reportGridInstance) {

        Set<ReportGridCell> tableData = reportGridInstance.cellData();

        Map<Long, Application> applicationsById = indexById(reportGridInstance.applications());
        Map<Long, RatingSchemeItem> ratingsById = indexById(reportGridInstance.ratingSchemeItems());

        Map<Long, Collection<ReportGridCell>> tableDataByAppId = groupBy(
                tableData,
                ReportGridCell::applicationId);

        boolean allowCostsExport = settingsService
                .getValue(SettingsService.ALLOW_COST_EXPORTS_KEY)
                .map(r -> StringUtilities.isEmpty(r) || Boolean.parseBoolean(r))
                .orElse(true);

        return tableDataByAppId
                .entrySet()
                .stream()
                .map(r -> {
                    Long appId = r.getKey();
                    Application app = applicationsById.getOrDefault(appId, null);

                    ArrayList<Object> reportRow = new ArrayList<>();

                    Collection<ReportGridCell> cells = r.getValue();

                    Map<Tuple3<Long, EntityKind, Long>, ReportGridCell> cellValuesByColumnRefForApp = indexBy(
                            cells,
                            k -> tuple(k.columnEntityId(), k.columnEntityKind(), k.entityFieldReferenceId()));

                    //find data for columns
                    colsWithCommentRequirement
                            .forEach(t -> {
                                ReportGridColumnDefinition colDef = t.v1;
                                Boolean needsComment = t.v2;

                                boolean isCostColumn = colDef.columnEntityKind().equals(EntityKind.COST_KIND);

                                if (!allowCostsExport && isCostColumn) {
                                    reportRow.add("REDACTED");
                                } else {
                                    ReportGridCell cell = cellValuesByColumnRefForApp.getOrDefault(
                                            tuple(
                                                    colDef.columnEntityId(),
                                                    colDef.columnEntityKind(),
                                                    getIdOrDefault(colDef.entityFieldReference(), null)),
                                            null);

                                    reportRow.add(getValueFromReportCell(ratingsById, cell));
                                    if (needsComment) {
                                        reportRow.add(getCommentFromCell(cell));
                                    }
                                }
                            });
                    return tuple(app, reportRow);
                })
                .sorted(Comparator.comparing(t -> t.v1.name()))
                .collect(toList());
    }


    private Object getCommentFromCell(ReportGridCell reportGridCell) {
        if (reportGridCell == null) {
            return null;
        }
        return reportGridCell.comment();
    }


    private Object getValueFromReportCell(Map<Long, RatingSchemeItem> ratingsById,
                                          ReportGridCell reportGridCell) {
        if (reportGridCell == null) {
            return null;
        }
        switch (reportGridCell.columnEntityKind()) {
            case COST_KIND:
                return reportGridCell.value();
            case INVOLVEMENT_KIND:
            case SURVEY_TEMPLATE:
            case APPLICATION:
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
                                                               Set<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions,
                                                               List<Tuple2<Application, ArrayList<Object>>> reportRows) throws IOException {
        switch (format) {
            case XLSX:
                return tuple(format, reportName, mkExcelReport(reportName, columnDefinitions, reportRows));
            case CSV:
                return tuple(format, reportName, mkCSVReport(columnDefinitions, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private byte[] mkCSVReport(Set<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions,
                               List<Tuple2<Application, ArrayList<Object>>> reportRows) throws IOException {
        List<String> headers = mkHeaderStrings(columnDefinitions);

        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    private List<Object> simplify(Tuple2<Application, ArrayList<Object>> row) {

        long appId = row.v1.entityReference().id();
        String appName = row.v1.name();
        Optional<ExternalIdValue> assetCode = row.v1.assetCode();
        LifecyclePhase lifecyclePhase = row.v1.lifecyclePhase();

        List<Object> appInfo = asList(appId, appName, assetCode, lifecyclePhase.name());

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
                                 Set<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions,
                                 List<Tuple2<Application, ArrayList<Object>>> reportRows) throws IOException {
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


    private int writeExcelBody(List<Tuple2<Application, ArrayList<Object>>> reportRows, SXSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(1);
        reportRows.forEach(r -> {

            long appId = r.v1.entityReference().id();
            String appName = r.v1.name();
            Optional<ExternalIdValue> assetCode = r.v1.assetCode();
            LifecyclePhase lifecyclePhase = r.v1.lifecyclePhase();

            List<Object> appInfo = asList(appId, appName, assetCode, lifecyclePhase.name());

            List<Object> values = concat(appInfo, r.v2);

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


    private int writeExcelHeader(Set<Tuple2<ReportGridColumnDefinition, Boolean>> columnDefinitions, SXSSFSheet sheet) {
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
