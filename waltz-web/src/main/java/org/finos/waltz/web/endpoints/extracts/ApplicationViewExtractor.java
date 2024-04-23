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
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.application.ApplicationsView;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.application.ApplicationViewService;
import org.finos.waltz.service.orgunit.OrganisationalUnitService;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.model.utils.IdUtilities.indexByOptionalId;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static spark.Spark.post;


@Service
public class ApplicationViewExtractor extends CustomDataExtractor {

    private final ApplicationViewService applicationViewService;
    private final OrganisationalUnitService orgUnitService;

    private final DSLContext dsl;

    @Autowired
    public ApplicationViewExtractor(DSLContext dsl,
                                    ApplicationViewService applicationViewService,
                                    OrganisationalUnitService orgUnitService) {
        this.dsl = dsl;
        this.applicationViewService = applicationViewService;
        this.orgUnitService = orgUnitService;
    }


    @Override
    public void register() {
        post(WebUtilities.mkPath("data-extract", "application", "by-selector"), (request, response) -> {
            IdSelectionOptions options = WebUtilities.readIdSelectionOptionsFromBody(request);

            return writeReportResults(
                    response,
                    prepareFlows(
                            options,
                            parseExtractFormat(request),
                            "applications"));
        });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareFlows(IdSelectionOptions options,
                                                               ExtractFormat format,
                                                               String reportName) throws IOException {

        ApplicationsView view = applicationViewService.getViewBySelector(options);

        Map<Optional<Long>, String> ouNameById = MapUtilities.indexBy(
                orgUnitService.findAll(),
                IdProvider::id,
                NameProvider::name);

        List<List<Object>> reportRows = prepareReportRows(view, ouNameById);

        List<String> staticHeaders = ListUtilities.asList(
                "Waltz Id",
                "Name",
                "Asset Code",
                "Application Kind",
                "Org Unit",
                "Lifecycle Phase");

        List<String> categoryHeaders = view
                .primaryRatings()
                .measurableCategories()
                .stream()
                .map(d -> String.format("Primary %s", d.name()))
                .sorted()
                .collect(toList());

        List<String> assessmentHeaders = view
                .primaryAssessments()
                .assessmentDefinitions()
                .stream()
                .map(NameProvider::name)
                .sorted()
                .collect(toList());

        return formatReport(
                format,
                reportName,
                reportRows,
                ListUtilities.concat(staticHeaders, categoryHeaders, assessmentHeaders));
    }


    private List<List<Object>> prepareReportRows(ApplicationsView viewData,
                                                 Map<Optional<Long>, String> ouNameById) {

        Map<Long, Measurable> measurablesById = indexByOptionalId(viewData.primaryRatings().measurables());
        Map<EntityReference, Collection<Tuple2<MeasurableRating, Measurable>>> entityToRatingsAndMeasurables = groupBy(
                viewData.primaryRatings().measurableRatings(),
                MeasurableRating::entityReference,
                mr -> tuple(mr, measurablesById.get(mr.measurableId())));

        Map<Long, RatingSchemeItem> ratingSchemeItemsById = viewData.primaryAssessments().ratingSchemeItemsById();
        Map<Tuple2<EntityReference, Long>, Collection<RatingSchemeItem>> assessmentsByEntityAndDefId = groupBy(
                viewData.primaryAssessments().assessmentRatings(),
                r -> tuple(r.entityReference(), r.assessmentDefinitionId()),
                r -> ratingSchemeItemsById.get(r.ratingId()));

        return viewData
                .applications()
                .stream()
                .map(app -> {
                    ArrayList<Object> reportRow = new ArrayList<>();

                    String ouName = ouNameById.getOrDefault(Optional.of(app.organisationalUnitId()), "?");

                    reportRow.add(app.id().get());
                    reportRow.add(app.name());
                    reportRow.add(app.assetCode());
                    reportRow.add(app.kind().prettyName());
                    reportRow.add(ouName);
                    reportRow.add(app.lifecyclePhase());

                    Collection<Tuple2<MeasurableRating, Measurable>> associatedPrimaryRatings = entityToRatingsAndMeasurables.getOrDefault(
                            app.entityReference(),
                            Collections.emptySet());

                    viewData.primaryRatings()
                            .measurableCategories()
                            .stream()
                            .sorted(Comparator.comparing(NameProvider::name))
                            .forEach(category -> {
                                String cellValue = associatedPrimaryRatings
                                        .stream()
                                        .filter(t -> Optional.of(t.v2.categoryId()).equals(category.id()))
                                        .findFirst()
                                        .map(t -> t.v2.name())
                                        .orElse(null);
                                reportRow.add(cellValue);
                            });

                    viewData.primaryAssessments()
                            .assessmentDefinitions()
                            .stream()
                            .sorted(Comparator.comparing(NameProvider::name))
                            .forEach(def -> {
                                Collection<RatingSchemeItem> ratings = assessmentsByEntityAndDefId.getOrDefault(
                                        tuple(app.entityReference(), def.id().get()),
                                        Collections.emptySet());

                                reportRow.add(ratings
                                        .stream()
                                        .map(NameProvider::name)
                                        .sorted()
                                        .collect(Collectors.joining(", ")));
                            });

                    return reportRow;
                })
                .collect(toList());
    }
}
