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
import org.finos.waltz.model.report_grid.ReportGridDerivedColumnDefinition;
import org.finos.waltz.model.report_grid.ReportGridFixedColumnDefinition;
import org.finos.waltz.web.endpoints.extracts.ColumnCommentary;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Component
public class FormatterUtils {


    public List<String> mkHeaderStrings(List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> fixedColumnDefinitions,
                                        List<ReportGridDerivedColumnDefinition> derivedColumnDefinitions) {
        List<String> staticHeaders = newArrayList(
                "Subject Id",
                "Subject Name",
                "Subject External Id",
                "Subject Lifecycle Phase");

        List<String> columnHeaders = mkColumnHeaders(fixedColumnDefinitions, derivedColumnDefinitions);

        return concat(
                staticHeaders,
                columnHeaders);
    }

    public List<String> mkColumnHeaders(List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> fixedColumnDefinitions,
                                        List<ReportGridDerivedColumnDefinition> derivedColumnDefinitions) {

        List<Tuple3<String, String, Integer>> fixedColumnHeaders = mkFixedColumnHeaders(fixedColumnDefinitions);
        List<Tuple3<String, String, Integer>> derivedColumnHeaders = mkDerivedColumnHeaders(derivedColumnDefinitions);

        return union(fixedColumnHeaders, derivedColumnHeaders)
                .stream()
                .sorted(Comparator.comparing(Tuple3::v3))
                .flatMap(t -> Stream.of(t.v1, t.v2))
                .filter(Objects::nonNull)
                .collect(toList());
    }


    public List<Tuple3<String, String, Integer>> mkFixedColumnHeaders(List<Tuple2<ReportGridFixedColumnDefinition, ColumnCommentary>> columnDefinitions) {
        if (columnDefinitions == null) {
            return new ArrayList<>();
        }
        return columnDefinitions
                .stream()
                .map(r -> {
                    String name = getColumnName(r.v1);
                    String commentName = ColumnCommentary.HAS_COMMENTARY.equals(r.v2)
                            ? String.format("%s: comment", name)
                            : null;

                    return tuple(name, commentName, r.v1.position());
                })
                .collect(toList());
    }


    public List<Tuple3<String, String, Integer>> mkDerivedColumnHeaders(List<ReportGridDerivedColumnDefinition> columnDefinitions) {
        if (columnDefinitions == null) {
            return new ArrayList<>();
        }
        return columnDefinitions
                .stream()
                .map(t -> tuple(t.displayName(), (String) null, t.position()))
                .collect(toList());
    }

    public String getColumnName(ReportGridFixedColumnDefinition column) {
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
