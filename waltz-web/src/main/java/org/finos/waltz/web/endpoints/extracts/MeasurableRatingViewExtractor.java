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
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable.MeasurableHierarchy;
import org.finos.waltz.model.measurable.MeasurableHierarchyAlignment;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.service.measurable_rating.MeasurableRatingViewService;
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
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.CollectionUtilities.notEmpty;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class MeasurableRatingViewExtractor extends CustomDataExtractor {

    private final MeasurableRatingViewService measurableRatingViewService;

    private final DSLContext dsl;

    @Autowired
    public MeasurableRatingViewExtractor(DSLContext dsl, MeasurableRatingViewService measurableRatingViewService) {
        this.dsl = dsl;
        this.measurableRatingViewService = measurableRatingViewService;
    }


    @Override
    public void register() {
        String viewForParentAndCategoryPath = mkPath("data-extract", "measurable-rating-view", "entity", ":kind", ":id", "category-id", ":categoryId");
        get(viewForParentAndCategoryPath, (request, response) -> {

            EntityReference parentRef = WebUtilities.getEntityReference(request);
            Long categoryId = WebUtilities.getLong(request, "categoryId");

            return writeReportResults(
                    response,
                    prepareFlows(
                            parentRef,
                            categoryId,
                            parseExtractFormat(request),
                            "measurable-ratings"));
        });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareFlows(EntityReference parentRef,
                                                               Long categoryId,
                                                               ExtractFormat format,
                                                               String reportName) throws IOException {

        MeasurableRatingCategoryView ratingView = measurableRatingViewService.getViewForAppAndCategory(parentRef, categoryId);

        Map<Long, MeasurableHierarchy> hierarchyByMeasurableId = indexBy(ratingView.measurableHierarchy(), MeasurableHierarchy::measurableId);
        Integer maxDepthOfTree = getMaxDepthOfTree(ratingView.ratings(), hierarchyByMeasurableId);

        List<List<Object>> reportRows = prepareReportRows(ratingView, maxDepthOfTree, hierarchyByMeasurableId);

        List<String> headers = prepareHeaders(ratingView, maxDepthOfTree);

        return formatReport(
                format,
                reportName,
                reportRows,
                headers
        );
    }

    private List<String> prepareHeaders(MeasurableRatingCategoryView ratingView, Integer maxDepthOfTree) {
        ArrayList<String> headerRow = new ArrayList<>();

        String categoryName = ratingView.category().name();

        headerRow.add("Primary?");

        int parentLevel = 1;
        while (parentLevel < maxDepthOfTree + 1) {
            headerRow.add(format("%s Lvl %d", categoryName, parentLevel));
            parentLevel = parentLevel + 1;
        }


        headerRow.add("Measurable External ID");
        headerRow.add("Rating");

        ratingView.allocationSchemes()
                .stream()
                .sorted(Comparator.comparing(NameProvider::name))
                .forEach(scheme -> headerRow.add(scheme.name()));

        if (notEmpty(ratingView.plannedDecommissions())) {
            headerRow.add("Planned Decommission Date");
        }

        if (notEmpty(ratingView.plannedReplacements())) {
            headerRow.add("Replacement Applications");
        }

        ratingView
                .assessmentDefinitions()
                .stream()
                .sorted(Comparator.comparing(NameProvider::name))
                .forEach(d -> headerRow.add(d.name()));

        return headerRow;
    }

    private List<List<Object>> prepareReportRows(MeasurableRatingCategoryView viewData,
                                                 Integer maxDepthOfTree,
                                                 Map<Long, MeasurableHierarchy> hierarchyByMeasurableId) {

        Map<Long, Measurable> measurablesById = indexById(viewData.measurables());
        Map<Long, Collection<Allocation>> allocationsByRatingId = groupBy(viewData.allocations(), Allocation::measurableRatingId);
        Map<String, RatingSchemeItem> ratingsByCode = viewData.ratingSchemeItemsByCode();
        Map<Long, MeasurableRatingPlannedDecommission> plannedDecommsByRatingId = indexBy(
                viewData.plannedDecommissions(),
                MeasurableRatingPlannedDecommission::measurableRatingId);
        Map<Long, Collection<String>> replacementsByDecommId = groupBy(
                viewData.plannedReplacements(),
                MeasurableRatingReplacement::decommissionId,
                d -> d.entityReference().name().get());
        Map<Long, Collection<AssessmentRating>> assessmentsByRatingId = groupBy(
                viewData.assessmentRatings(),
                d -> d.entityReference().id());

        return viewData
                .ratings()
                .stream()
                .map(row -> {

                    ArrayList<Object> reportRow = new ArrayList<>();

                    RatingSchemeItem rating = ratingsByCode.get(String.valueOf(row.rating()));

                    Collection<Allocation> allocs = allocationsByRatingId.getOrDefault(row.id().get(), Collections.emptySet());
                    Map<Long, Integer> allocationsBySchemeId = indexBy(allocs, Allocation::schemeId, Allocation::percentage);

                    Measurable measurable = measurablesById.get(row.measurableId());

                    MeasurableHierarchy measurableHierarchy = hierarchyByMeasurableId.get(row.measurableId());

                    Map<Integer, String> parentsByLevel = indexBy(
                            measurableHierarchy.parents(),
                            MeasurableHierarchyAlignment::level,
                            d -> d.parentReference().name().orElse("-"));

                    String primary = row.isPrimary() ? "Y" : "N";
                    reportRow.add(primary);

                    int parentLevel = 1;
                    while (parentLevel < maxDepthOfTree + 1) {
                        String parent = parentsByLevel.getOrDefault(parentLevel, "-");
                        reportRow.add(parent);
                        parentLevel++;
                    }

                    reportRow.add(measurable.entityReference().externalId());
                    reportRow.add(rating.name());

                    viewData.allocationSchemes()
                            .stream()
                            .sorted(Comparator.comparing(NameProvider::name))
                            .forEach(scheme -> {
                                Integer allocation = allocationsBySchemeId.get(scheme.id().get());
                                String allocationString = Optional.ofNullable(allocation)
                                        .map(String::valueOf)
                                        .orElse("Unallocated");
                                reportRow.add(allocationString);
                            });

                    MeasurableRatingPlannedDecommission decomm = plannedDecommsByRatingId.get(row.id().get());

                    if (notEmpty(viewData.plannedDecommissions())){
                        String decommString = Optional.ofNullable(decomm)
                                .map(d -> String.valueOf(d.plannedDecommissionDate()))
                                .orElse(null);
                        reportRow.add(decommString);
                    }

                    if (notEmpty(viewData.plannedReplacements())){
                        Collection<String> replacements = Optional.ofNullable(decomm)
                                .map(d -> replacementsByDecommId.getOrDefault(d.id(), Collections.emptyList()))
                                .orElse(Collections.emptyList());
                        reportRow.add(StringUtilities.join(replacements, ", "));
                    }

                    Collection<AssessmentRating> assessmentRatings = assessmentsByRatingId.getOrDefault(row.id().get(), Collections.emptyList());

                    Map<Long, RatingSchemeItem> ratingsByDefinitionId = indexBy(assessmentRatings,
                            AssessmentRating::assessmentDefinitionId,
                            d -> viewData.ratingSchemeItemsById().get(d.ratingId()));

                    viewData.assessmentDefinitions()
                            .stream()
                            .sorted(Comparator.comparing(NameProvider::name))
                            .forEach(d -> {
                                String assessmentRating = Optional.ofNullable(ratingsByDefinitionId.get(d.id().get()))
                                        .map(NameProvider::name)
                                        .orElse(null);
                                reportRow.add(assessmentRating);
                            });

                    return reportRow;
                })
                .collect(toList());
    }

    private Integer getMaxDepthOfTree(Set<MeasurableRating> ratings,
                                      Map<Long, MeasurableHierarchy> hierarchyByMeasurableId) {
        return ratings
                .stream()
                .map(r -> {
                    MeasurableHierarchy measurableHierarchy = hierarchyByMeasurableId.get(r.measurableId());
                    return measurableHierarchy.maxDepth();
                })
                .max(Comparator.naturalOrder())
                .orElse(1);
    }
}
