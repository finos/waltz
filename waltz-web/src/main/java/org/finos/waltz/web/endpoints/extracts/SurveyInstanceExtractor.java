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
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.survey.SurveyQuestionDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.web.WebUtilities;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import spark.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.EnumUtilities.names;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.fromArray;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.schema.Tables.APPLICATION;
import static org.finos.waltz.schema.Tables.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.tables.SurveyInstance.SURVEY_INSTANCE;
import static org.finos.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;
import static org.finos.waltz.schema.tables.SurveyRun.SURVEY_RUN;
import static org.finos.waltz.schema.tables.SurveyTemplate.SURVEY_TEMPLATE;
import static org.finos.waltz.web.endpoints.extracts.ExtractorUtilities.sanitizeSheetName;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.get;

@Service
public class SurveyInstanceExtractor implements DataExtractor {

    public static final String BASE_URL = WebUtilities.mkPath("data-extract", "survey-instance");
    private final DSLContext dsl;
    private final SurveyQuestionDao questionDao;
    private final org.finos.waltz.schema.tables.SurveyTemplate st = SURVEY_TEMPLATE.as("st");
    private final org.finos.waltz.schema.tables.SurveyRun sr = SURVEY_RUN.as("sr");
    private final org.finos.waltz.schema.tables.SurveyInstance si = SURVEY_INSTANCE.as("si");
    private final org.finos.waltz.schema.tables.SurveyQuestionResponse sqr = SURVEY_QUESTION_RESPONSE.as("sqr");
    private final org.finos.waltz.schema.tables.Application app = APPLICATION.as("app");
    private final org.finos.waltz.schema.tables.ChangeInitiative ci = CHANGE_INITIATIVE.as("ci");
    private final org.finos.waltz.schema.tables.Application sqapp = APPLICATION.as("sqapp");
    private final org.finos.waltz.schema.tables.Person sqp = PERSON.as("sqp");

    private static final Logger LOG = LoggerFactory.getLogger(SurveyInstanceExtractor.class);

    private final Field<String> subjectNameField =  DSL.coalesce(app.NAME, ci.NAME);
    private final Field<String> subjectExtIdField = DSL.coalesce(app.ASSET_CODE, ci.EXTERNAL_ID);

    private final Field<String> responseNameField = DSL.coalesce(sqapp.NAME, sqp.DISPLAY_NAME);
    private final Field<String> responseExtIdField = DSL.coalesce(sqapp.ASSET_CODE, sqp.EMPLOYEE_ID);


    @Autowired
    public SurveyInstanceExtractor(DSLContext dsl,
                                   SurveyQuestionDao questionDao) {
        this.dsl = dsl;
        this.questionDao = questionDao;
    }


    @Override
    public void register() {
        registerTemplateBasedExtract();
        registerRunBasedExtract();

    }


    private void registerRunBasedExtract() {
        get(WebUtilities.mkPath(BASE_URL, "run-id", ":id"),
            (request, response) -> writeReportResults(
                    response,
                    prepareInstancesOfRun(
                            parseExtractFormat(request),
                            WebUtilities.getId(request),
                            parseStatuses(request))));
    }



    private void registerTemplateBasedExtract() {
        get(WebUtilities.mkPath(BASE_URL, "template-id", ":id"),
            (request, response) -> writeReportResults(
                    response,
                    prepareInstancesOfTemplate(
                        parseExtractFormat(request),
                        WebUtilities.getId(request),
                        parseStatuses(request))));
    }


    private Set<SurveyInstanceStatus> parseStatuses(Request request) {
        Set<String> strStatuses = SetUtilities.fromArray(request.queryParamsValues("status"));

        return strStatuses.isEmpty()
                ? fromArray(SurveyInstanceStatus.values())
                : SetUtilities.map(strStatuses, SurveyInstanceStatus::valueOf);
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareInstancesOfTemplate(ExtractFormat format,
                                                                             long templateId,
                                                                             Set<SurveyInstanceStatus> statuses) throws IOException {
        String reportName = loadTemplateName(templateId);

        List<SurveyQuestion> questions = loadQuestions(templateId);
        List<List<Object>> reportRows = prepareReportRowsForTemplate(questions, templateId, statuses);

        return formatReport(
                format,
                reportName,
                questions,
                reportRows);
    }


    private Tuple3<ExtractFormat, String,byte[]> prepareInstancesOfRun(ExtractFormat format,
                                                                       long runId,
                                                                       Set<SurveyInstanceStatus> statuses) throws IOException {
        Record2<String, Long> r = dsl
                .select(sr.NAME, sr.SURVEY_TEMPLATE_ID)
                .from(sr.where(sr.ID.eq(runId)))
                .fetchOne();

        String reportName = r.component1();
        long templateId = r.component2();

        List<SurveyQuestion> questions = loadQuestions(templateId);
        List<List<Object>> reportRows = prepareReportRowsForRun(questions, runId, statuses);

        return formatReport(
                format,
                reportName,
                questions,
                reportRows);
    }


    private Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               String reportName,
                                                               List<SurveyQuestion> questions,
                                                               List<List<Object>> reportRows) throws IOException {

        LOG.info("Formatting {} rows of data into {} format", reportRows.size(), format.name());

        switch (format) {
            case XLSX:
                return tuple(format, reportName, mkExcelReport(reportName, questions, reportRows));
            case CSV:
                return tuple(format, reportName, mkCSVReport(questions, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private byte[] mkCSVReport(List<SurveyQuestion> questions,
                               List<List<Object>> reportRows) throws IOException {
        List<String> headers = mkHeaderStrings(questions);

        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    private List<String> mkHeaderStrings(List<SurveyQuestion> questions) {
        List<String> staticHeaders = newArrayList(
                    "NAME",
                    "RUN ID",
                    "RUN STATUS",
                    "SURVEY ID",
                    "SURVEY STATUS",
                    "SUBJECT_NAME",
                    "SUBJECT_EXT_ID",
                    "SUBMITTED_AT",
                    "SUBMITTED_BY",
                    "APPROVED_AT",
                    "APPROVED_BY",
                    "Latest",
                    "EXTERNAL_ID");

        List<String> questionHeaders = questions
                .stream()
                .flatMap(q -> {
                    Stream.Builder<String> builder = Stream.builder();
                    builder.add(q.questionText());
                    if (q.allowComment()) builder.add(q.questionText() + " (Commentary)");
                    return builder.build();
                })
                .collect(toList());

        return concat(
                staticHeaders,
                questionHeaders);
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


    private byte[] mkExcelReport(String reportName, List<SurveyQuestion> questions, List<List<Object>> reportRows) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = workbook.createSheet(sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(questions, sheet);
        writeExcelBody(reportRows, sheet);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colCount));
        sheet.createFreezePane(0, 1);

        return convertExcelToByteArray(workbook);
    }


    private byte[] convertExcelToByteArray(SXSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
        workbook.write(outByteStream);
        workbook.close();
        return outByteStream.toByteArray();
    }


    private int writeExcelBody(List<List<Object>> reportRows, SXSSFSheet sheet) {
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


    private int writeExcelHeader(List<SurveyQuestion> questions, SXSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();

        mkHeaderStrings(questions).forEach(hdr -> writeExcelHeaderCell(headerRow, colNum, hdr));

        return colNum.get();
    }


    private void writeExcelHeaderCell(Row headerRow, AtomicInteger colNum, String text) {
        Cell cell = headerRow.createCell(colNum.getAndIncrement());
        cell.setCellValue(text);
    }


    private List<List<Object>> prepareReportRowsForTemplate(List<SurveyQuestion> questions, long templateId, Set<SurveyInstanceStatus> statuses) {

        Condition condition = st.ID.eq(templateId)
                .and(si.STATUS.in(names(statuses)));

        return prepareReportRows(questions, condition);
    }


    private List<List<Object>> prepareReportRowsForRun(List<SurveyQuestion> questions, long runId, Set<SurveyInstanceStatus> statuses) {

        Condition condition = sr.ID.eq(runId)
                .and(si.STATUS.in(names(statuses)));

        return prepareReportRows(questions, condition);
    }



    private List<List<Object>> prepareReportRows(List<SurveyQuestion> questions, Condition condition) {


        SelectConditionStep<Record> instanceQuery = dsl
                .select(sr.NAME,
                        sr.ID,
                        sr.STATUS.as("run_status"))
                .select(subjectNameField,
                        subjectExtIdField)
                .select(si.ID,
                        si.STATUS.as("instance_status"),
                        si.APPROVED_AT,
                        si.APPROVED_BY,
                        si.SUBMITTED_AT,
                        si.SUBMITTED_BY)
                .select(DSL.when(si.ORIGINAL_INSTANCE_ID.isNull(), "Yes").otherwise("No").as("Latest"))
                .select(st.EXTERNAL_ID)
                .from(st)
                .innerJoin(sr).on(sr.SURVEY_TEMPLATE_ID.eq(st.ID))
                .innerJoin(si).on(si.SURVEY_RUN_ID.eq(sr.ID))
                .leftJoin(app).on(si.ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(si.ENTITY_ID.eq(app.ID)))
                .leftJoin(ci).on(si.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()).and(si.ENTITY_ID.eq(ci.ID)))
                .where(dsl.renderInlined(condition));


        SelectConditionStep<Record> responseQuery = dsl
                .select(si.ID)
                .select(sqr.QUESTION_ID,
                        sqr.COMMENT)
                .select(sqr.STRING_RESPONSE,
                        sqr.NUMBER_RESPONSE,
                        sqr.DATE_RESPONSE,
                        sqr.BOOLEAN_RESPONSE,
                        sqr.LIST_RESPONSE_CONCAT)
                .select(responseNameField,
                        responseExtIdField)
                .from(st)
                .innerJoin(sr).on(sr.SURVEY_TEMPLATE_ID.eq(st.ID))
                .innerJoin(si).on(si.SURVEY_RUN_ID.eq(sr.ID))
                .leftJoin(sqr).on(sqr.SURVEY_INSTANCE_ID.eq(si.ID))
                .leftJoin(sqapp).on(sqr.ENTITY_RESPONSE_KIND.eq(EntityKind.APPLICATION.name()).and(sqr.ENTITY_RESPONSE_ID.eq(sqapp.ID)))
                .leftJoin(sqp).on(sqr.ENTITY_RESPONSE_KIND.eq(EntityKind.PERSON.name()).and(sqr.ENTITY_RESPONSE_ID.eq(sqp.ID)))
                .where(dsl.renderInlined(condition));

        Result<Record> instanceResults = instanceQuery.fetch();
        Map<Long, Result<Record>> responseResults = responseQuery.fetchGroups(si.ID);

        return instanceResults
                    .stream()
                    .map(instance -> {
                        ArrayList<Object> reportRow = new ArrayList<>();

                        //These fields are shared between all the questions for a survey but
                        // aiming for only one row per instance so only need to take one row
                        reportRow.add(instance.get(sr.NAME));
                        reportRow.add(instance.get(sr.ID));
                        reportRow.add(instance.get("run_status", String.class));
                        reportRow.add(instance.get(si.ID));
                        reportRow.add(instance.get("instance_status", String.class));
                        reportRow.add(instance.get(subjectNameField));
                        reportRow.add(instance.get(subjectExtIdField));
                        reportRow.add(instance.get(si.SUBMITTED_AT));
                        reportRow.add(instance.get(si.SUBMITTED_BY));
                        reportRow.add(instance.get(si.APPROVED_AT));
                        reportRow.add(instance.get(si.APPROVED_BY));
                        reportRow.add(instance.get("Latest"));
                        reportRow.add(instance.get(st.EXTERNAL_ID));

                        List<Record> responsesForInstance = responseResults.get(instance.get(si.ID));

                        if (responsesForInstance != null) {
                            Map<Long, Record> answersByQuestionId = indexBy(
                                    responsesForInstance,
                                    a -> a.get(sqr.QUESTION_ID));

                            questions
                                    .stream()
                                    .map(q -> tuple(q, answersByQuestionId.get(q.id().get())))
                                    .forEach(t -> {
                                        reportRow.add(findValueInRecord(t.v1, t.v2));
                                        if (t.v1.allowComment()) {
                                            reportRow.add(getComment(t));
                                        }
                                    });
                        }

                        return reportRow;
                    })
                    .collect(toList());
    }


    private String getComment(Tuple2<SurveyQuestion, Record> t) {
        return (t.v2 == null)
                ? ""
                : mkSafe(t.v2.get(sqr.COMMENT));
    }


    private Object findValueInRecord(SurveyQuestion q, Record r) {
        if (r == null) {
            return "";
        }
        switch (q.fieldType()) {
            case NUMBER:
                return r.get(sqr.NUMBER_RESPONSE);
            case BOOLEAN:
                return r.get(sqr.BOOLEAN_RESPONSE);
            case DATE:
                return r.get(sqr.DATE_RESPONSE);
            case DROPDOWN_MULTI_SELECT:
                return r.get(sqr.LIST_RESPONSE_CONCAT);
            case APPLICATION:
            case PERSON:
                return ofNullable(r.get(responseNameField))
                        .map(name -> name + " (" + r.get(responseExtIdField) + ")")
                        .orElse(null);
            default:
                return r.get(sqr.STRING_RESPONSE);
        }
    }


    private String loadTemplateName(long templateId) {
        return dsl
                .select(st.NAME)
                .from(st)
                .where(st.ID.eq(templateId))
                .fetchOne()
                .component1();
    }


    private List<SurveyQuestion> loadQuestions(Long templateId) {
        return questionDao.findForTemplate(templateId);
    }


}
