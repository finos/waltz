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

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.entity_field_reference.EntityFieldReference;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.finos.waltz.web.endpoints.extracts.ColumnCommentary;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.ListUtilities.newArrayList;

@Component
public class FormatterUtils {


    public List<String> mkHeaderStrings(List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions) {
        List<String> staticHeaders = newArrayList(
                "Subject Id",
                "Subject Name",
                "Subject External Id",
                "Subject Lifecycle Phase");
        List<String> columnHeaders = mkColumnHeaders(columnDefinitions);
        return concat(
                staticHeaders,
                columnHeaders);
    }


    public List<String> mkColumnHeaders(List<Tuple2<ReportGridColumnDefinition, ColumnCommentary>> columnDefinitions){
        if(columnDefinitions==null){
            return new ArrayList<>();
        }
        return columnDefinitions
                .stream()
                .sorted(Comparator.comparingLong(r -> r.v1.position()))
                .flatMap(r -> {
                    String name = getColumnName(r.v1);
                    return Stream.of(
                            name,
                            ColumnCommentary.HAS_COMMENTARY.equals(r.v2)
                                    ? String.format("%s: comment", name)
                                    : null);

                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public String getColumnName(ReportGridColumnDefinition column) {
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

}
