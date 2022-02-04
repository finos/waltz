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
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.attestation.ApplicationAttestationInstanceInfo;
import org.finos.waltz.model.attestation.ApplicationAttestationInstanceSummary;
import org.finos.waltz.model.external_identifier.ExternalIdValue;
import org.finos.waltz.service.attestation.AttestationInstanceService;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static org.finos.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static org.finos.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;
import static org.finos.waltz.web.endpoints.extracts.ExtractorUtilities.convertExcelToByteArray;
import static org.finos.waltz.web.endpoints.extracts.ExtractorUtilities.sanitizeSheetName;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.get;
import static spark.Spark.post;


@Service
public class AttestationExtractor extends DirectQueryBasedDataExtractor {

    private final AttestationInstanceService attestationInstanceService;

    public AttestationExtractor(DSLContext dsl,
                                AttestationInstanceService attestationInstanceService) {
        super(dsl);
        this.attestationInstanceService = attestationInstanceService;
    }


    @Override
    public void register() {
        registerExtractForRun(WebUtilities.mkPath(
                "data-extract",
                "attestation",
                ":id"));
        registerExtractForAttestedEntityKindAndSelector();
    }


    private void registerExtractForAttestedEntityKindAndSelector() {
        post(WebUtilities.mkPath("data-extract", "attestations", ":kind", ":id"),
                (request, response) -> {
                    Long attestedId = StringUtilities.parseLong(request.params("id"), null);

                    return writeReportResults(
                            response,
                            prepareReport(
                                    parseExtractFormat(request),
                                    WebUtilities.getKind(request),
                                    attestedId,
                                    WebUtilities.readBody(request, ApplicationAttestationInstanceInfo.class)));
                });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareReport(ExtractFormat format,
                                                                EntityKind attestedKind,
                                                                Long attestedId,
                                                                ApplicationAttestationInstanceInfo applicationAttestationInstanceInfo) throws IOException {

        Set<ApplicationAttestationInstanceSummary> attestationInstances = attestationInstanceService
                .findApplicationAttestationInstancesForKindAndSelector(
                        attestedKind,
                        attestedId,
                        applicationAttestationInstanceInfo);

        EntityReference entityReference = applicationAttestationInstanceInfo.selectionOptions().entityReference();

        String fileName = format(
                "%s-attestations-for-%s-%s",
                attestedKind,
                entityReference.kind().name().toLowerCase(),
                entityReference.id());

        List<String> columnDefinitions = ListUtilities.asList(
                "Application Id",
                "Application Name",
                "Asset Code",
                "Criticality",
                "Lifecycle Phase",
                "Kind",
                "Attested At",
                "Attested By");

        return formatReport(
                format,
                fileName,
                columnDefinitions,
                attestationInstances);
    }


    private void registerExtractForRun(String path) {
        get(path, (request, response) -> {
            long runId = WebUtilities.getId(request);

            String runName = dsl
                    .select(ATTESTATION_RUN.NAME)
                    .from(ATTESTATION_RUN)
                    .where(ATTESTATION_RUN.ID.eq(runId))
                    .fetchOne(ATTESTATION_RUN.NAME);

            checkNotNull(runName, "AttestationRun cannot be null");
            String suggestedFilename = runName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            return writeExtract(
                    suggestedFilename,
                    prepareExtractQuery(runId),
                    request,
                    response);
        });
    }


    private SelectConditionStep<Record> prepareExtractQuery(long runId) {
        return dsl
                .select(APPLICATION.NAME.as("Application"),
                        APPLICATION.ASSET_CODE.as("External Id"))
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_KIND.as("Attesting Kind"),
                        ATTESTATION_RUN.ATTESTED_ENTITY_ID.as("Attesting Kind Id"))
                .select(ATTESTATION_INSTANCE.ID.as("Attestation Id"),
                        ATTESTATION_INSTANCE.ATTESTED_BY.as("Attested By"),
                        ATTESTATION_INSTANCE.ATTESTED_AT.as("Attested At"))
                .select(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.as("Recipient"))
                .from(ATTESTATION_INSTANCE)
                .join(ATTESTATION_INSTANCE_RECIPIENT)
                    .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .join(ATTESTATION_RUN)
                    .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .join(APPLICATION)
                    .on(APPLICATION.ID.eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID))
                .where(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(runId))
                    .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
    }


    private Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               String reportName,
                                                               List<String> columns,
                                                               Set<ApplicationAttestationInstanceSummary> reportRows) throws IOException {
        switch (format) {
            case XLSX:
                return tuple(format, reportName, mkExcelReport(reportName, columns, reportRows));
            case CSV:
                return tuple(format, reportName, mkCSVReport(columns, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private byte[] mkCSVReport(List<String> columnDefinitions,
                               Set<ApplicationAttestationInstanceSummary> reportRows) throws IOException {

        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(columnDefinitions);

        reportRows.forEach(unchecked(r -> {
            List<Object> values = asList(
                    r.appRef().id(),
                    r.appRef().name().get(),
                    r.appAssetCode(),
                    r.appCriticality(),
                    r.appLifecyclePhase(),
                    r.appKind(),
                    r.attestedAt(),
                    r.attestedBy());

            csvWriter.write(values);
        }));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    private byte[] mkExcelReport(String reportName,
                                 List<String> columnDefinitions,
                                 Set<ApplicationAttestationInstanceSummary> reportRows) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = workbook.createSheet(sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(columnDefinitions, sheet);
        writeExcelBody(reportRows, sheet);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, colCount - 1));
        sheet.createFreezePane(0, 1);

        return convertExcelToByteArray(workbook);
    }


    private int writeExcelBody(Set<ApplicationAttestationInstanceSummary> reportRows, SXSSFSheet sheet) {
        AtomicInteger rowNum = new AtomicInteger(1);
        reportRows.forEach(r -> {

            List<Object> values = asList(
                    r.appRef().id(),
                    r.appRef().name().get(),
                    r.appAssetCode(),
                    r.appCriticality(),
                    r.appLifecyclePhase(),
                    r.appKind(),
                    r.attestedAt(),
                    r.attestedBy());

            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum.getAndIncrement());
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


    private int writeExcelHeader(List<String> columnDefinitions, SXSSFSheet sheet) {
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();

        columnDefinitions.forEach(hdr -> writeExcelHeaderCell(headerRow, colNum, hdr));

        return colNum.get();
    }


    private void writeExcelHeaderCell(Row headerRow, AtomicInteger colNum, String text) {
        Cell cell = headerRow.createCell(colNum.getAndIncrement());
        cell.setCellValue(text);
    }

}
