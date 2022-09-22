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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.web.endpoints.extracts.ColumnCommentary;
import org.finos.waltz.web.json.*;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class DynamicJSONFormatter implements DynamicFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicJSONFormatter.class);
    private final FormatterUtils formatterUtils;


    public DynamicJSONFormatter(FormatterUtils formatterUtils){
        this.formatterUtils = formatterUtils;
    }


    @Override
    public byte[] format(String id,
                         ReportGrid reportGrid,
                         List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions,
                         List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows)  throws IOException {
        try {
            LOG.debug("Generating JSON data {}",id);
            long start = System.currentTimeMillis();
            byte[] response = mkResponse(reportGrid, columnDefinitions, reportRows);
            long finish = System.currentTimeMillis();
            LOG.info(
                    "Generated JSON data {} in {}ms response. response payload sz={}bytes",
                    id,
                    finish-start,
                    response.length);
            return response;
        } catch (IOException e) {
           String err = String.format(
                   "Encountered error generating JSON response.Details:%s",
                   e.getMessage());
           throw new IOException(err,e);
        }
    }


    private byte[] mkResponse(ReportGrid reportGrid,
                              List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions,
                              List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {

        ReportGridDefinition reportGridDefinition = reportGrid.definition();
        ReportGridJSON reportGridJSON =
                ImmutableReportGridJSON.builder()
                        .id(reportGridDefinition.externalId().orElseGet(() -> "" + reportGridDefinition.id()))
                        .apiTypes(new ApiTypes())
                        .name(reportGridDefinition.name())
                        .grid(transform(columnDefinitions, reportGrid.definition().derivedColumnDefinitions(), reportRows))
                        .build();

        return createMapper()
                .writeValueAsBytes(reportGridJSON);
    }


    private Grid transform(List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> fixedColumnDefinitions,
                           List<ReportGridDerivedColumnDefinition> derivedColumnDefinitions,
                           List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) {

        List<String> columnHeadings = formatterUtils.mkColumnHeaders(fixedColumnDefinitions, derivedColumnDefinitions);

        List<Row> data = new ArrayList<>(reportRows.size());

        for (Tuple2<ReportSubject, ArrayList<Object>> currentRow : reportRows) {
            ImmutableRow.Builder transformedRow = ImmutableRow.builder();

            List<CellValue> transformedRowValues = new ArrayList<>();

            transformedRow.id(createKeyElement(currentRow.v1));

            int maxColumns = columnHeadings.size();

            for (int idx = 0; idx < maxColumns; idx++) {
                String formattedColumnName = columnHeadings.get(idx) != null
                        ? columnHeadings.get(idx)
                        : "";
                int prevCellAddedIdx = transformedRowValues.size() - 1;
                boolean isComment = formattedColumnName.contains("comment");
                Object currentCell = currentRow.v2.get(idx);
                if (currentCell != null) {
                    CellValue cell = ImmutableCellValue
                            .builder()
                            .name(formattedColumnName)
                            .value(currentCell.toString())
                            .build();

                    if (isComment && prevCellAddedIdx > -1 && transformedRowValues.get(prevCellAddedIdx) instanceof ImmutableCellValue) {
                        CellValue previousColumnCell = transformedRowValues.get(prevCellAddedIdx);
                        CellValue withComment = ImmutableCellValue
                                .copyOf(previousColumnCell)
                                .withComment(currentCell.toString());
                        transformedRowValues.set(prevCellAddedIdx,withComment);
                    } else {
                        transformedRowValues.add(cell);
                    }

                }
            }
            transformedRow.addAllCells(transformedRowValues);
            data.add(transformedRow.build());
        }

        return ImmutableGrid.builder()
                .addAllRows(data)
                .build();
    }


    private KeyCell createKeyElement(ReportSubject reportSubject){
        return KeyCell.fromSubject(reportSubject);
    }


    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

}
