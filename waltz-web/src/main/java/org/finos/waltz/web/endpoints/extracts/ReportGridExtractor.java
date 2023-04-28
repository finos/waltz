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

import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.service.report_grid.ReportGridService;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.web.WebException;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.extracts.reportgrid.DynamicCommaSeperatedValueFormatter;
import org.finos.waltz.web.endpoints.extracts.reportgrid.DynamicExcelFormatter;
import org.finos.waltz.web.endpoints.extracts.reportgrid.DynamicJSONFormatter;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.finos.waltz.service.report_grid.ReportGridColumnCalculator.calculate;
import static org.finos.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;

@Service
public class ReportGridExtractor implements SupportsJsonExtraction {

    private static final String BASE_URL = WebUtilities.mkPath("data-extract", "report-grid");

    private final DynamicCommaSeperatedValueFormatter dynamicCommaSeperatedValueFormatter;
    private final DynamicExcelFormatter dynamicExcelFormatter;
    private final DynamicJSONFormatter dynamicJSONFormatter;
    private final ReportGridService reportGridService;
    private final SurveyQuestionService surveyQuestionService;
    private final SettingsService settingsService;


    @Autowired
    public ReportGridExtractor(DynamicCommaSeperatedValueFormatter dynamicCommaSeperatedValueFormatter,
                               DynamicExcelFormatter dynamicExcelFormatter,
                               DynamicJSONFormatter dynamicJSONFormatter,
                               ReportGridService reportGridService,
                               SurveyQuestionService surveyQuestionService,
                               SettingsService settingsService) {

        this.dynamicCommaSeperatedValueFormatter = dynamicCommaSeperatedValueFormatter;
        this.dynamicExcelFormatter = dynamicExcelFormatter;
        this.dynamicJSONFormatter = dynamicJSONFormatter;
        this.reportGridService = reportGridService;
        this.surveyQuestionService = surveyQuestionService;
        this.settingsService = settingsService;
    }


    @Override
    public void register() {
        registerGridViewExtractByExternalId();
    }


    private void registerGridViewExtractByExternalId() {
        post(WebUtilities.mkPath(BASE_URL, "external-id", ":externalId"),
                (request, response) -> {
                    String externalId = request.params("externalId");
                    IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);

                    Optional<ReportGridDefinition> definition =
                            reportGridService.findByExternalId(externalId);

                    return definition
                            .map(def-> {
                                try {
                                    long reportGridIdentifier = def
                                            .id()
                                            .orElseThrow(() -> new IllegalArgumentException("Report Grid Definition found but it has no internal identifier"));

                                    return findReportGridById(reportGridIdentifier, selectionOptions)
                                            .map(Unchecked.function(reportGrid -> prepareReport(
                                                    reportGrid,
                                                    parseExtractFormat(request),
                                                    selectionOptions)))
                                            .map(Unchecked.function(report -> writeReportResults(
                                                    response,
                                                    report)))
                                            .orElseThrow(() -> notFoundException.apply(reportGridIdentifier));

                                } catch(UncheckedIOException e) {
                                    throw new WebException("REPORT_GRID_RENDER_ERROR", mkSafe(e.getMessage()), e);
                                }
                            })
                            .orElseThrow(() -> new NotFoundException(
                                "MISSING_GRID",
                                String.format(
                                        "Report Grid GUID (%s) not found",
                                        externalId)));
                });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareReport(ReportGrid reportGrid,
                                                                ExtractFormat format,
                                                                IdSelectionOptions selectionOptions) throws IOException {

        List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> colsWithCommentRequirement = enrichColsWithCommentRequirement(reportGrid);

        List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows = prepareReportRows(
                reportGrid.definition(),
                colsWithCommentRequirement,
                reportGrid.instance());

        return formatReport(
                format,
                reportGrid,
                mkReportName(reportGrid.definition(), selectionOptions),
                colsWithCommentRequirement,
                reportRows);
    }


    private Optional<ReportGrid> findReportGridById(long gridId,
                                                    IdSelectionOptions selectionOptions){
        return reportGridService.getByIdAndSelectionOptions(
                gridId,
                selectionOptions,
                "extractor-user");
    }


    private List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> enrichColsWithCommentRequirement(ReportGrid reportGrid) {
        return reportGrid
                .definition()
                .id()
                .map(reportGridService::findCommentSupportingColumnIdsForGrid)
                .map(commentableColIds -> reportGrid
                        .definition()
                        .fixedColumnDefinitions()
                        .stream()
                        .map(cd -> tuple(
                                cd,
                                commentableColIds.contains(cd.id())
                                    ? ColumnCommentary.HAS_COMMENTARY
                                    : ColumnCommentary.NO_COMMENTARY))
                        .sorted(comparingLong(r -> r.v1.position()))
                        .collect(toList()))
                .orElseThrow(() -> new IllegalStateException("grid has no id"));
    }


    private String mkReportName(ReportGridDefinition gridDefinition, IdSelectionOptions selectionOptions) {
        return format("%s_%s_%s",
                gridDefinition.name(),
                selectionOptions.entityReference().kind(),
                selectionOptions.entityReference().id());
    }


    private List<Tuple2<ReportSubject, ArrayList<Object>>> prepareReportRows(ReportGridDefinition definition,
                                                                             List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> colsWithCommentRequirement,
                                                                             ReportGridInstance reportGridInstance) {

        List<Tuple3<Long, Integer, EntityKind>> derivedCols = map(definition.derivedColumnDefinitions(), d -> tuple(d.gridColumnId(), d.position(), d.kind()));
        List<Tuple3<Long, Integer, EntityKind>> fixedCols = map(definition.fixedColumnDefinitions(), d -> tuple(d.gridColumnId(), d.position(), d.kind()));

        Map<Long, Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> colsWithCommentReqById = indexBy(
                colsWithCommentRequirement,
                k -> k.v1.id());

        Set<Tuple3<Long, Integer, EntityKind>> allColumnDefinitions = union(derivedCols, fixedCols);

        Set<ReportGridCell> tableData = (definition.derivedColumnDefinitions().isEmpty())
                ? reportGridInstance.cellData()
                : union(reportGridInstance.cellData(), calculate(reportGridInstance, definition));

        Map<Long, RatingSchemeItem> ratingsById = indexById(reportGridInstance.ratingSchemeItems());

        Map<Long, Collection<ReportGridCell>> tableDataBySubjectId = groupBy(
                tableData,
                ReportGridCell::subjectId);

        boolean allowCostsExport = settingsService
                .getValue(SettingsService.ALLOW_COST_EXPORTS_KEY)
                .map(r -> StringUtilities.isEmpty(r) || Boolean.parseBoolean(r))
                .orElse(true);

        return reportGridInstance
                .subjects()
                .stream()
                .map(subject -> {
                    Collection<ReportGridCell> cellsForSubject = tableDataBySubjectId.getOrDefault(
                            subject.entityReference().id(),
                            emptySet());

                    ArrayList<Object> reportRow = new ArrayList<>();

                    Map<Long, ReportGridCell> cellValuesByColumnRefForSubject = indexBy(
                            cellsForSubject,
                            ReportGridCell::columnDefinitionId);

                    allColumnDefinitions
                            .stream()
                            .sorted(Comparator.comparingInt(Tuple3::v2))
                            .forEachOrdered(t -> {

                                Long columnId = t.v1;

                                if (t.v3.equals(EntityKind.REPORT_GRID_DERIVED_COLUMN_DEFINITION)) {

                                    ReportGridCell cell = cellValuesByColumnRefForSubject.getOrDefault(
                                            columnId,
                                            null);

                                    reportRow.add(getDerivedCellValue(cell));

                                } else {

                                    Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary> fixedCol = colsWithCommentReqById.get(columnId);
                                    ReportGridFixedColumnDefinition colDef = fixedCol.v1;

                                    boolean isCostColumn = colDef.columnEntityKind().equals(EntityKind.COST_KIND);

                                    if (!allowCostsExport && isCostColumn) {
                                        reportRow.add("REDACTED");
                                    } else {

                                        ReportGridCell cell = cellValuesByColumnRefForSubject.getOrDefault(
                                                colDef.gridColumnId(),
                                                null);

                                        reportRow.add(getValueFromFixedReportCell(colDef, ratingsById, cell));

                                        if (ColumnCommentary.HAS_COMMENTARY.equals(fixedCol.v2)) {
                                            reportRow.add(getCommentFromCell(cell));
                                        }
                                    }
                                }
                            });

                    return tuple(subject, reportRow);
                })
                .sorted(Comparator.comparing(t -> t.v1.entityReference().name().get()))
                .collect(toList());
    }

    private Object getDerivedCellValue(ReportGridCell cell) {
        if (cell == null) {
            return null;
        } else {
            return Optional
                    .ofNullable(cell.textValue())
                    .orElse(cell.errorValue());
        }
    }

    private Object getCommentFromCell(ReportGridCell reportGridCell) {
        if (reportGridCell == null) {
            return null;
        }
        return reportGridCell.comment();
    }


    private Object getValueFromFixedReportCell(ReportGridFixedColumnDefinition colDef,
                                               Map<Long, RatingSchemeItem> ratingsById,
                                               ReportGridCell reportGridCell) {
        if (reportGridCell == null) {
            return null;
        }
        switch (colDef.columnEntityKind()) {
            case COST_KIND:
            case COMPLEXITY_KIND:
                return reportGridCell.numberValue();
            case INVOLVEMENT_KIND:
            case SURVEY_TEMPLATE:
            case APPLICATION:
            case CHANGE_INITIATIVE:
            case SURVEY_QUESTION:
            case DATA_TYPE:
            case APP_GROUP:
            case ORG_UNIT:
            case TAG:
            case ENTITY_ALIAS:
            case MEASURABLE_CATEGORY:
            case ENTITY_STATISTIC:
                return Optional
                        .ofNullable(reportGridCell.textValue())
                        .orElse("-");
            case ATTESTATION:
                return Optional
                        .ofNullable(reportGridCell.dateTimeValue())
                        .map(LocalDateTime::toString)
                        .orElse("-");
            case MEASURABLE:
            case ASSESSMENT_DEFINITION:
                return reportGridCell.textValue();
            default:
                throw new IllegalArgumentException("This report does not support export with column of type: " + colDef.columnEntityKind().name());
        }
    }


    private Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               ReportGrid reportGrid,
                                                               String reportName,
                                                               List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions,
                                                               List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
        switch (format) {
            case XLSX:
                return tuple(format, reportName, dynamicExcelFormatter
                        .format(reportName, reportGrid, columnDefinitions, reportRows));
            case CSV:
                return tuple(format, reportName, dynamicCommaSeperatedValueFormatter
                        .format(reportName, reportGrid, columnDefinitions, reportRows));
            case JSON:
                return tuple(format, reportName, dynamicJSONFormatter
                        .format(reportName, reportGrid, columnDefinitions, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private static final LongFunction<NotFoundException> notFoundException = (gridId) -> new NotFoundException(
            "REPORT_GRID_NOT_FOUND",
            format(" Grid def: %d not found", gridId));


}
