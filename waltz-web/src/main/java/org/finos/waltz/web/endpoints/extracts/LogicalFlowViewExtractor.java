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

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.datatype.DataTypeDecorator;
import org.finos.waltz.model.logical_flow.LogicalFlowView;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.StringUtilities.joinUsing;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static spark.Spark.post;


@Service
public class LogicalFlowViewExtractor extends CustomDataExtractor {

    private final LogicalFlowService logicalFlowService;

    private final DSLContext dsl;

    @Autowired
    public LogicalFlowViewExtractor(DSLContext dsl, LogicalFlowService logicalFlowService) {
        this.dsl = dsl;
        this.logicalFlowService = logicalFlowService;
    }


    @Override
    public void register() {
        post(WebUtilities.mkPath("data-extract", "logical-flow-view"), (request, response) -> {
            IdSelectionOptions options = WebUtilities.readIdSelectionOptionsFromBody(request);

            return writeReportResults(
                    response,
                    prepareFlows(
                            options,
                            parseExtractFormat(request),
                            "logical-flows"));
        });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareFlows(IdSelectionOptions options, ExtractFormat format,
                                                               String reportName) throws IOException {

        LogicalFlowView flowView = logicalFlowService.getFlowView(options);

        List<List<Object>> reportRows = prepareReportRows(flowView);

        List<String> staticHeaders = ListUtilities.asList(
                "Source Entity Name",
                "Source Entity External Id",
                "Target Entity Name",
                "Target Entity External Id",
                "Data Types",
                "Physical Flow Count");

        List<String> assessmentHeaders = flowView.logicalFlowAssessmentDefinitions()
                .stream()
                .map(d -> d.name())
                .sorted()
                .collect(toList());

        return formatReport(
                format,
                reportName,
                reportRows,
                ListUtilities.concat(staticHeaders, assessmentHeaders)
        );
    }

    private List<List<Object>> prepareReportRows(LogicalFlowView viewData) {

        Map<Long, Collection<DataTypeDecorator>> logicalFlowDataTypesByFlowId = MapUtilities.groupBy(viewData.logicalFlowDataTypeDecorators(), DataTypeDecorator::dataFlowId);
        Map<Long, Collection<PhysicalFlow>> physicalsByLogicalFlowId = MapUtilities.groupBy(viewData.physicalFlows(), PhysicalFlow::logicalFlowId);
        Map<Long, Collection<AssessmentRating>> ratingsByFlowId = MapUtilities.groupBy(viewData.logicalFlowRatings(), d -> d.entityReference().id());
        Map<Long, RatingSchemeItem> ratingSchemeItemsById = indexById(viewData.ratingSchemeItems());

        return viewData
                .logicalFlows()
                .stream()
                .map(row -> {
                    ArrayList<Object> reportRow = new ArrayList<>();

                    Collection<DataTypeDecorator> decorators = logicalFlowDataTypesByFlowId.getOrDefault(row.entityReference().id(), Collections.emptySet());
                    String dataTypeString = joinUsing(decorators, d -> d.decoratorEntity().name().orElseGet(() -> "?"), ", ");

                    Map<Long, Collection<AssessmentRating>> ratingsByDefnId = MapUtilities.groupBy(
                            ratingsByFlowId.getOrDefault(row.entityReference().id(), Collections.emptySet()),
                            AssessmentRating::assessmentDefinitionId);

                    Collection<PhysicalFlow> physicals = physicalsByLogicalFlowId.getOrDefault(row.entityReference().id(), Collections.emptySet());

                    reportRow.add(row.source().name());
                    reportRow.add(row.source().externalId());
                    reportRow.add(row.target().name());
                    reportRow.add(row.target().externalId());
                    reportRow.add(dataTypeString);
                    reportRow.add(physicals.size());

                    viewData.logicalFlowAssessmentDefinitions()
                            .stream()
                            .sorted(Comparator.comparing(NameProvider::name))
                            .forEach(defn -> {
                                String ratingsStrForDefn = ratingsByDefnId.getOrDefault(defn.id().get(), Collections.emptySet())
                                        .stream()
                                        .map(d -> ratingSchemeItemsById.getOrDefault(d.ratingId(), null))
                                        .filter(Objects::nonNull)
                                        .map(NameProvider::name)
                                        .sorted()
                                        .collect(Collectors.joining(", ")); //may have multivalued assessments

                                reportRow.add(ratingsStrForDefn);
                            });

                    return reportRow;
                })
                .collect(toList());
    }
}
