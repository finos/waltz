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
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.service.report_grid.ReportGridService;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.extracts.reportgrid.DynamicCommaSeperatedValueFormatter;
import org.finos.waltz.web.endpoints.extracts.reportgrid.DynamicExcelFormatter;
import org.finos.waltz.web.endpoints.extracts.reportgrid.DynamicJSONFormatter;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.LongFunction;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.MapUtilities.*;
import static org.finos.waltz.model.utils.IdUtilities.getIdOrDefault;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;

@Service
public class ReportGridExtractor implements DataExtractor {

    public static final String BASE_URL = WebUtilities.mkPath("data-extract", "report-grid");
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
        registerGridViewExtract();
    }


    private void registerGridViewExtract() {
        post(WebUtilities.mkPath(BASE_URL, "id", ":id"),
            (request, response) -> {
                long gridId = WebUtilities.getId(request);
                IdSelectionOptions selectionOptions =  WebUtilities.readIdSelectionOptionsFromBody(request);
                ReportGrid reportGrid = getById(gridId,selectionOptions)
                        .orElseThrow(() -> notFoundException.apply(gridId));
                return writeReportResults(
                    response,
                    prepareReport(reportGrid,
                        parseExtractFormat(request),
                        selectionOptions));
            });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareReport(ReportGrid reportGrid,
                                                                ExtractFormat format,
                                                                IdSelectionOptions selectionOptions) throws IOException {

        List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> colsWithCommentRequirement = enrichColsWithCommentRequirement(reportGrid);

        List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows = prepareReportRows(colsWithCommentRequirement, reportGrid.instance());

        return formatReport(
                format,
                reportGrid,
                mkReportName(reportGrid.definition(), selectionOptions),
                colsWithCommentRequirement,
                reportRows);
    }

    private Optional<ReportGrid> getById(long gridId,
                                         IdSelectionOptions selectionOptions){
        return reportGridService.getByIdAndSelectionOptions(
                gridId,
                selectionOptions);
    }

    private List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> enrichColsWithCommentRequirement(ReportGrid reportGrid) {
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
                .map(cd -> tuple(cd, columnHasComment(cd,colsNeedingComments)))
                .sorted(Comparator.comparingLong(r -> r.v1.position()))
                .collect(toList());
    }

    private static ColumnCommentary columnHasComment(ReportGridColumnDefinition cd, Set<Long> colsNeedingComments ){
       return (colsNeedingComments.contains(cd.columnEntityId())) ?
               ColumnCommentary.HAS_COMMENTARY:ColumnCommentary.NO_COMMENTARY;
    }

    private String mkReportName(ReportGridDefinition gridDefinition, IdSelectionOptions selectionOptions) {
        return format("%s_%s_%s",
                gridDefinition.name(),
                selectionOptions.entityReference().kind(),
                selectionOptions.entityReference().id());
    }



    private List<Tuple2<ReportSubject, ArrayList<Object>>> prepareReportRows(List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> colsWithCommentRequirement,
                                                                             ReportGridInstance reportGridInstance) {

        Set<ReportGridCell> tableData = reportGridInstance.cellData();

        Map<Long, ReportSubject> subjectsById = indexBy(reportGridInstance.subjects(), d -> d.entityReference().id());
        Map<Long, RatingSchemeItem> ratingsById = indexById(reportGridInstance.ratingSchemeItems());

        Map<Long, Collection<ReportGridCell>> tableDataBySubjectId = groupBy(
                tableData,
                ReportGridCell::subjectId);

        boolean allowCostsExport = settingsService
                .getValue(SettingsService.ALLOW_COST_EXPORTS_KEY)
                .map(r -> StringUtilities.isEmpty(r) || Boolean.parseBoolean(r))
                .orElse(true);

        return tableDataBySubjectId
                .entrySet()
                .stream()
                .map(r -> {
                    Long subjectId = r.getKey();

                    ReportSubject subject = subjectsById.getOrDefault(subjectId, null);

                    ArrayList<Object> reportRow = new ArrayList<>();

                    Collection<ReportGridCell> cells = r.getValue();

                    Map<Tuple3<Long, EntityKind, Long>, ReportGridCell> cellValuesByColumnRefForSubject = indexBy(
                            cells,
                            k -> tuple(k.columnEntityId(), k.columnEntityKind(), k.entityFieldReferenceId()));

                    //find data for columns
                    colsWithCommentRequirement
                            .forEach(t -> {
                                ReportGridColumnDefinition colDef = t.v1;
                                boolean isCostColumn = colDef.columnEntityKind().equals(EntityKind.COST_KIND);

                                if (!allowCostsExport && isCostColumn) {
                                    reportRow.add("REDACTED");
                                } else {
                                    ReportGridCell cell = cellValuesByColumnRefForSubject.getOrDefault(
                                            tuple(
                                                    colDef.columnEntityId(),
                                                    colDef.columnEntityKind(),
                                                    getIdOrDefault(colDef.entityFieldReference(), null)),
                                            null);

                                    reportRow.add(getValueFromReportCell(ratingsById, cell));
                                    if (ColumnCommentary.HAS_COMMENTARY.equals(t.v2)) {
                                        reportRow.add(getCommentFromCell(cell));
                                    }
                                }
                            });

                    return tuple(subject, reportRow);
                })
                .sorted(Comparator.comparing(t -> t.v1.entityReference().name().get()))
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
            case CHANGE_INITIATIVE:
            case SURVEY_QUESTION:
            case DATA_TYPE:
            case APP_GROUP:
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
                                                               ReportGrid reportGrid,
                                                               String reportName,
                                                               List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions,
                                                               List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
        switch (format) {
            case XLSX:
                return tuple(format, reportName, dynamicExcelFormatter
                        .format(reportName, reportGrid,columnDefinitions, reportRows));
            case CSV:
                return tuple(format, reportName, dynamicCommaSeperatedValueFormatter
                        .format(reportName,reportGrid,columnDefinitions, reportRows));
            case JSON:
                return tuple(format, reportName, dynamicJSONFormatter
                        .format(reportName,reportGrid,columnDefinitions, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private static final LongFunction<NotFoundException> notFoundException = (gridId) -> new NotFoundException(
            "REPORT_GRID_NOT_FOUND",
            format(" Grid def: %d not found", gridId));
}
