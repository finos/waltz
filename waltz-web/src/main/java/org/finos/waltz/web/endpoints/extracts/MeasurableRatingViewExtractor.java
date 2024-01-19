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

import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.allocation.Allocation;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.application.AssessmentsView;
import org.finos.waltz.model.application.MeasurableRatingsView;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.model.measurable.MeasurableHierarchy;
import org.finos.waltz.model.measurable.MeasurableHierarchyAlignment;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.model.measurable_rating.AllocationsView;
import org.finos.waltz.model.measurable_rating.DecommissionsView;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.finos.waltz.model.measurable_rating.MeasurableRatingCategoryView;
import org.finos.waltz.model.measurable_rating.MeasurableRatingViewParams;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.utils.IdUtilities;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
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
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.CollectionUtilities.notEmpty;
import static org.finos.waltz.common.CollectionUtilities.sort;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.join;
import static org.finos.waltz.model.utils.IdUtilities.indexById;
import static org.finos.waltz.web.WebUtilities.mkPath;
import static org.finos.waltz.web.WebUtilities.readBody;
import static spark.Spark.post;


@Service
public class MeasurableRatingViewExtractor extends CustomDataExtractor {

    private final MeasurableRatingViewService measurableRatingViewService;
    private final MeasurableCategoryService measurableCategoryService;

    private final DSLContext dsl;

    @Autowired
    public MeasurableRatingViewExtractor(DSLContext dsl, MeasurableRatingViewService measurableRatingViewService, MeasurableCategoryService measurableCategoryService) {
        this.dsl = dsl;
        this.measurableRatingViewService = measurableRatingViewService;
        this.measurableCategoryService = measurableCategoryService;
    }


    @Override
    public void register() {
        String viewForParentAndCategoryPath = mkPath("data-extract", "measurable-rating-view", "category-id", ":categoryId");

        post(viewForParentAndCategoryPath, (request, response) -> {

            MeasurableRatingViewParams viewParams = readBody(request, MeasurableRatingViewParams.class);
            Long categoryId = WebUtilities.getLong(request, "categoryId");

            MeasurableCategory category = measurableCategoryService.getById(categoryId);
            String fileName = format("%s Ratings", category.name());

            return writeReportResults(
                    response,
                    prepareFlows(
                            viewParams,
                            categoryId,
                            parseExtractFormat(request),
                            fileName));
        });
    }


    private Tuple3<ExtractFormat, String, byte[]> prepareFlows(MeasurableRatingViewParams viewParams,
                                                               Long categoryId,
                                                               ExtractFormat format,
                                                               String reportName) throws IOException {

        MeasurableRatingCategoryView view = measurableRatingViewService.getViewForCategoryAndSelector(viewParams.idSelectionOptions(), categoryId);

        MeasurableRatingsView measurableRatingsView = view.measurableRatings();
        Map<Long, MeasurableHierarchy> hierarchyByMeasurableId = indexBy(measurableRatingsView.measurableHierarchy(), MeasurableHierarchy::measurableId);
        Integer maxDepthOfTree = getMaxDepthOfTree(measurableRatingsView.measurableRatings(), hierarchyByMeasurableId);

        List<List<Object>> reportRows = prepareReportRows(view, maxDepthOfTree, hierarchyByMeasurableId, viewParams.parentMeasurableId());

        List<String> headers = prepareHeaders(view, maxDepthOfTree);

        return formatReport(
                format,
                reportName,
                reportRows,
                headers
        );
    }

    private List<String> prepareHeaders(MeasurableRatingCategoryView view, Integer maxDepthOfTree) {
        ArrayList<String> headerRow = new ArrayList<>();

        MeasurableRatingsView measurableRatingsView = view.measurableRatings();
        AllocationsView allocationsView = view.allocations();
        DecommissionsView decommissionsView = view.decommissions();
        AssessmentsView assessmentsView = view.primaryAssessments();

        Set<MeasurableCategory> measurableCategories = measurableRatingsView.measurableCategories();
        MeasurableCategory measurableCategory = first(measurableCategories);

        String categoryName = measurableCategory.name();

        headerRow.add("Primary?");

        headerRow.add("Application");
        headerRow.add("Application Asset Code");

        int parentLevel = 1;
        while (parentLevel < maxDepthOfTree + 1) {
            headerRow.add(format("%s Lvl %d", categoryName, parentLevel));
            parentLevel = parentLevel + 1;
        }


        headerRow.add("Measurable External ID");
        headerRow.add("Rating");

        allocationsView
                .allocationSchemes()
                .stream()
                .sorted(comparing(NameProvider::name))
                .forEach(scheme -> headerRow.add(scheme.name()));

        if (notEmpty(decommissionsView.plannedDecommissions())) {
            headerRow.add("Planned Decommission Date");
        }

        if (notEmpty(decommissionsView.plannedReplacements())) {
            headerRow.add("Replacement Applications");
        }

        assessmentsView
                .assessmentDefinitions()
                .stream()
                .sorted(comparing(NameProvider::name))
                .forEach(d -> headerRow.add(d.name()));

        return headerRow;
    }

    private List<List<Object>> prepareReportRows(MeasurableRatingCategoryView view,
                                                 Integer maxDepthOfTree,
                                                 Map<Long, MeasurableHierarchy> hierarchyByMeasurableId,
                                                 Optional<Long> parentMeasurableId) {

        MeasurableRatingsView measurableRatingsView = view.measurableRatings();
        AllocationsView allocationsView = view.allocations();
        DecommissionsView decommissionsView = view.decommissions();
        AssessmentsView assessmentsView = view.primaryAssessments();

        Map<Long, Measurable> measurablesById = indexById(measurableRatingsView.measurables());
        Map<Long, Collection<Allocation>> allocationsByRatingId = groupBy(allocationsView.allocations(), Allocation::measurableRatingId);
        Map<String, RatingSchemeItem> ratingsByCode = indexBy(measurableRatingsView.ratingSchemeItems(), RatingSchemeItem::rating);
        Map<Long, Application> appsById = IdUtilities.indexByOptionalId(view.applications());

        Map<Long, MeasurableRatingPlannedDecommission> plannedDecommsByRatingId = indexBy(
                decommissionsView.plannedDecommissions(),
                MeasurableRatingPlannedDecommission::measurableRatingId);

        Map<Long, Collection<String>> replacementsByDecommId = groupBy(
                decommissionsView.plannedReplacements(),
                MeasurableRatingReplacement::decommissionId,
                d -> d.entityReference().name().get());

        Map<Long, Collection<AssessmentRating>> assessmentsByRatingId = groupBy(
                assessmentsView.assessmentRatings(),
                d -> d.entityReference().id());

        return measurableRatingsView
                .measurableRatings()
                .stream()
                .filter(mkParentMeasurableFilter(hierarchyByMeasurableId, parentMeasurableId))
                .map(row -> {

                    ArrayList<Object> reportRow = new ArrayList<>();

                    RatingSchemeItem rating = ratingsByCode.get(String.valueOf(row.rating()));
                    Application application = appsById.get(row.entityReference().id());

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

                    reportRow.add(application.name());
                    reportRow.add(application.assetCode());

                    int parentLevel = 1;
                    while (parentLevel < maxDepthOfTree + 1) {
                        String parent = parentsByLevel.getOrDefault(parentLevel, "-");
                        reportRow.add(parent);
                        parentLevel++;
                    }

                    reportRow.add(measurable.entityReference().externalId());
                    reportRow.add(rating.name());

                    allocationsView
                            .allocationSchemes()
                            .stream()
                            .sorted(comparing(NameProvider::name))
                            .forEach(scheme -> {
                                Integer allocation = allocationsBySchemeId.get(scheme.id().get());
                                String allocationString = Optional.ofNullable(allocation)
                                        .map(String::valueOf)
                                        .orElse("Unallocated");
                                reportRow.add(allocationString);
                            });

                    MeasurableRatingPlannedDecommission decomm = plannedDecommsByRatingId.get(row.id().get());

                    if (notEmpty(decommissionsView.plannedDecommissions())){
                        String decommString = Optional.ofNullable(decomm)
                                .map(d -> String.valueOf(d.plannedDecommissionDate()))
                                .orElse(null);
                        reportRow.add(decommString);
                    }

                    if (notEmpty(decommissionsView.plannedReplacements())){
                        Collection<String> replacements = Optional.ofNullable(decomm)
                                .map(d -> replacementsByDecommId.getOrDefault(d.id(), Collections.emptyList()))
                                .orElse(Collections.emptyList());
                        reportRow.add(join(sort(replacements, comparing(String::toLowerCase)), ", "));
                    }

                    Collection<AssessmentRating> assessmentRatings = assessmentsByRatingId.getOrDefault(row.id().get(), Collections.emptyList());

                    Map<Long, RatingSchemeItem> ratingsByDefinitionId = indexBy(assessmentRatings,
                            AssessmentRating::assessmentDefinitionId,
                            d -> assessmentsView.ratingSchemeItemsById().get(d.ratingId()));

                    assessmentsView
                            .assessmentDefinitions()
                            .stream()
                            .sorted(comparing(NameProvider::name))
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


    private Predicate<MeasurableRating> mkParentMeasurableFilter(Map<Long, MeasurableHierarchy> hierarchyByMeasurableId, Optional<Long> parentMeasurableId) {
        return d -> parentMeasurableId
                .map(mId -> {
                    MeasurableHierarchy measurableHierarchy = hierarchyByMeasurableId.get(d.measurableId());
                    Set<Long> parentIds = map(measurableHierarchy.parents(), parent -> parent.parentReference().id());
                    return parentIds.contains(mId);
                })
                .orElse(true);
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
