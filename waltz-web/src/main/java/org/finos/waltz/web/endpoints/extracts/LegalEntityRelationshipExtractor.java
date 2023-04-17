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
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipView;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipViewAssessment;
import org.finos.waltz.model.legal_entity.LegalEntityRelationshipViewRow;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.CollectionUtilities.sort;
import static org.finos.waltz.common.ListUtilities.*;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.endpoints.extracts.ExtractorUtilities.sanitizeSheetName;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;

@Service
public class LegalEntityRelationshipExtractor implements DataExtractor {

    public static final String BASE_URL = mkPath("data-extract", "legal-entity-relationship");
    private final DSLContext dsl;
    private final LegalEntityRelationshipService relationshipService;
    private final RatingSchemeService ratingSchemeService;
    private static final Logger LOG = LoggerFactory.getLogger(LegalEntityRelationshipExtractor.class);


    @Autowired
    public LegalEntityRelationshipExtractor(DSLContext dsl,
                                            LegalEntityRelationshipService relationshipService,
                                            RatingSchemeService ratingSchemeService) {
        this.dsl = dsl;
        this.relationshipService = relationshipService;
        this.ratingSchemeService = ratingSchemeService;
    }


    @Override
    public void register() {
        legalEntityRelationshipViewExtract();

    }


    private void legalEntityRelationshipViewExtract() {
        post(mkPath(BASE_URL, "relationship-kind", ":id"),
                (request, response) -> writeReportResults(
                        response,
                        prepareRows(
                                parseExtractFormat(request),
                                WebUtilities.getId(request),
                                WebUtilities.readIdSelectionOptionsFromBody(request))));
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareRows(ExtractFormat format,
                                                              long relKindId, IdSelectionOptions selectionOptions) throws IOException {

        LegalEntityRelationshipView relationshipsView = relationshipService.getViewByRelKindAndSelector(relKindId, selectionOptions);
        List<EntityReference> headers = sort(relationshipsView.assessmentHeaders(), comparing(d -> d.name().get()));
        Map<Long, RatingSchemeItem> ratingSchemeItemById = indexBy(
                ratingSchemeService.findAllRatingSchemeItems(),
                d -> d.id().get());

        List<List<Object>> reportRows = prepareReportRows(relationshipsView.rows(), headers, ratingSchemeItemById);

        return formatReport(
                format,
                "legal-entity-relationships",
                headers,
                reportRows);
    }



    private Tuple3<ExtractFormat, String, byte[]> formatReport(ExtractFormat format,
                                                               String reportName,
                                                               List<EntityReference> definitions,
                                                               List<List<Object>> reportRows) throws IOException {

        LOG.info("Formatting {} rows of data into {} format", reportRows.size(), format.name());

        switch (format) {
            case XLSX:
                return tuple(format, reportName, mkExcelReport(reportName, definitions, reportRows));
            case CSV:
                return tuple(format, reportName, mkCSVReport(definitions, reportRows));
            default:
                throw new UnsupportedOperationException("This report does not support export format: " + format);
        }
    }


    private byte[] mkCSVReport(List<EntityReference> definitions,
                               List<List<Object>> reportRows) throws IOException {
        List<String> headers = mkHeaderStrings(definitions);

        StringWriter writer = new StringWriter();
        CsvListWriter csvWriter = new CsvListWriter(writer, CsvPreference.EXCEL_PREFERENCE);

        csvWriter.write(headers);
        reportRows.forEach(unchecked(row -> csvWriter.write(simplify(row))));
        csvWriter.flush();

        return writer.toString().getBytes();
    }


    private List<String> mkHeaderStrings(List<EntityReference> definitions) {

        List<String> staticHeaders = newArrayList(
                "Target Entity Name",
                "Target Entity External Id",
                "Legal Entity Name",
                "Legal Entity External Id",
                "Description",
                "Last Updated At",
                "Last Updated By");

        List<String> assessmentHeaders = definitions
                .stream()
                .map(h -> h.name().orElse(format("Unknown Definition, id: %d", h.id())))
                .collect(toList());

        return concat(
                staticHeaders,
                assessmentHeaders);
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


    private byte[] mkExcelReport(String reportName, List<EntityReference> definitions, List<List<Object>> reportRows) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(2000);
        SXSSFSheet sheet = workbook.createSheet(sanitizeSheetName(reportName));

        int colCount = writeExcelHeader(definitions, sheet);
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


    private int writeExcelHeader(List<EntityReference> definitions, SXSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        AtomicInteger colNum = new AtomicInteger();

        mkHeaderStrings(definitions).forEach(hdr -> writeExcelHeaderCell(headerRow, colNum, hdr));

        return colNum.get();
    }


    private void writeExcelHeaderCell(Row headerRow, AtomicInteger colNum, String text) {
        Cell cell = headerRow.createCell(colNum.getAndIncrement());
        cell.setCellValue(text);
    }


    private List<List<Object>> prepareReportRows(Set<LegalEntityRelationshipViewRow> rows,
                                                 List<EntityReference> headers, Map<Long, RatingSchemeItem> ratingSchemeItemsById) {

        Comparator<RatingSchemeItem> comparator = Comparator
                .comparingInt(RatingSchemeItem::position)
                .thenComparing(RatingSchemeItem::name);

        return rows
                .stream()
                .sorted(Comparator.comparing(r -> mkTargetAndLENameComparatorString(r)))
                .map(row -> {
                    ArrayList<Object> reportRow = new ArrayList<>();

                    reportRow.add(row.relationship().targetEntityReference().name());
                    reportRow.add(row.relationship().targetEntityReference().externalId());
                    reportRow.add(row.relationship().legalEntityReference().name());
                    reportRow.add(row.relationship().legalEntityReference().externalId());
                    reportRow.add(row.relationship().description());
                    reportRow.add(row.relationship().lastUpdatedAt());
                    reportRow.add(row.relationship().lastUpdatedBy());

                    Map<Long, String> ratingOutcomesByDefinition = indexBy(
                            row.assessments(),
                            LegalEntityRelationshipViewAssessment::assessmentDefinitionId,
                            d -> d.ratingIds()
                                    .stream()
                                    .map(ratingSchemeItemsById::get)
                                    .filter(Objects::nonNull)
                                    .sorted(comparator)
                                    .map(NameProvider::name)
                                    .collect(Collectors.joining("; ")));

                    headers
                            .forEach(t -> reportRow.add(ratingOutcomesByDefinition.get(t.id())));

                    return reportRow;
                })
                .collect(toList());
    }

    private String mkTargetAndLENameComparatorString(LegalEntityRelationshipViewRow r) {
        return format("%s_%s", r.relationship().targetEntityReference().name().orElse(""), r.relationship().legalEntityReference().name().orElse(""));
    }


}
