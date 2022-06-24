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
package org.finos.waltz.web.endpoints.extracts.dynamic;

import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.report_grid.ReportGrid;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.finos.waltz.model.report_grid.ReportSubject;
import org.finos.waltz.web.endpoints.extracts.ColumnCommentary;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.*;
import static org.jooq.lambda.fi.util.function.CheckedConsumer.unchecked;

@Component
public class DynamicCommaSeperatedValueFormatter implements DynamicFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicCommaSeperatedValueFormatter.class);
    private final FormatterUtils formatterUtils;

    public DynamicCommaSeperatedValueFormatter(FormatterUtils formatterUtils){
        this.formatterUtils = formatterUtils;
    }


    @Override
    public byte[] format(String id,
                         ReportGrid reportGrid,
                         List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions,
                         List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException{
        try {
            LOG.info("Generating CSV report {}",id);
            return mkCSVReport(columnDefinitions,reportRows);
        } catch (IOException e) {
           LOG.warn("Encounter error when trying to generate CSV report.  Details:{}", e.getMessage());
           throw e;
        }
    }

    private byte[] mkCSVReport(List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions,
                               List<Tuple2<ReportSubject, ArrayList<Object>>> reportRows) throws IOException {
        List<String> headers = formatterUtils.mkHeaderStrings(columnDefinitions);

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


}
