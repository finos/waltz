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
import _ from "lodash";
import {CORE_API} from "../common/services/core-api-utils";
import {mkSelectionOptions} from "../common/selector-utils";
import {lastViewedMeasurableCategoryKey} from "../user";
import {reduceToSelectedNodesOnly} from "../common/hierarchy-utils";
import {entity} from "../common/services/enums/entity";
import {editOperations} from "../common/services/enums/operation";


export function loadAllData(
    $q,
    serviceBroker,
    parentEntityRef,
    force = false) {

    const applicationPromise = serviceBroker
        .loadViewData(CORE_API.ApplicationStore.getById, [parentEntityRef.id])
        .then(r => ({ application: r.data}));

    const categoriesPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableCategoryStore.findPopulatedCategoriesForRef,
                [parentEntityRef],
                {force})
            .then(r => ({ categories: r.data}));

    const lastViewedCategoryPromise = serviceBroker
        .loadAppData(CORE_API.UserPreferenceStore.findAllForUser, [], {force: true})
        .then(r => {
            const lastViewed = _.find(r.data, d => d.key === lastViewedMeasurableCategoryKey);
            return {
                lastViewedCategoryId: lastViewed
                    ? Number(lastViewed.value)
                    : null
            };
        });

    return $q
        .all([
            applicationPromise,
            categoriesPromise,
            lastViewedCategoryPromise
        ])
        .then(results => Object.assign({}, ...results));
}


function prepareTabForCategory(measurableRatingsView,
                               allocationsView,
                               decommissionsView,
                               assessmentsView,
                               application,
                               showAllMeasurables = true) {

    const ratedMeasurables = _.map(measurableRatingsView.measurableRatings, d => d.measurableId);
    const replacingMeasurables = _.map(decommissionsView.replacingDecommissions, d => d.measurableRating.measurableId);

    const requiredMeasurables = _.concat(ratedMeasurables, replacingMeasurables);

    const measurableRatingSchemeItemsByCode = _.keyBy(measurableRatingsView.ratingSchemeItems, d => d.rating);
    const assessmentRatingSchemeItemsById = _.keyBy(assessmentsView.ratingSchemeItems, d => d.id);

    const measurableHierarchyById = _.keyBy(measurableRatingsView.measurableHierarchy, d => d.measurableId);

    const measurables = showAllMeasurables
        ? measurableRatingsView.measurables
        : reduceToSelectedNodesOnly(measurableRatingsView.measurables, requiredMeasurables);

    const ratings = _
        .chain(measurableRatingsView.measurableRatings)
        .map(r => Object.assign(r, {ratingSchemeItem: measurableRatingSchemeItemsByCode[r.rating]}))
        .value();

    const assessmentRatings = _
        .chain(assessmentsView.assessmentRatings)
        .map(r => Object.assign(r, {ratingSchemeItem: assessmentRatingSchemeItemsById[r.ratingId]}))
        .value();

    const allocationTotalsByScheme = _
        .chain(allocationsView.allocations)
        .groupBy(d => d.schemeId)
        .mapValues(d => _.sumBy(d, a => a.percentage))
        .value();

    const plannedDecomms = _.isNull(application.plannedRetirementDate)
        ? _.map(decommissionsView.plannedDecommissions, d => Object.assign({}, d, { isValid: true}))
        : _.map(decommissionsView.plannedDecommissions, d => {

            const decomDate = new Date(d.plannedDecommissionDate);
            const appRetirementDate = new Date(application.plannedRetirementDate);

            const sameDate = appRetirementDate.getFullYear() === decomDate.getFullYear()
                && appRetirementDate.getMonth() === decomDate.getMonth()
                && appRetirementDate.getDate() === decomDate.getDate();

            const isValid = appRetirementDate > decomDate || sameDate;

            return Object.assign({}, d, {isValid: isValid})
        });


    return {
        category: _.head(measurableRatingsView.measurableCategories),
        measurables,
        ratings,
        allocationSchemes: allocationsView.allocationSchemes,
        allocations: allocationsView.allocations,
        assessmentDefinitions: assessmentsView.assessmentDefinitions,
        assessmentRatings,
        plannedDecommissions: plannedDecomms,
        plannedReplacements: decommissionsView.plannedReplacements,
        replacingDecommissions: decommissionsView.replacingDecommissions,
        ratingSchemeItems: measurableRatingsView.ratingSchemeItems,
        allocationTotalsByScheme,
        measurableHierarchyById
    };
}

export function mkTab(ctx, application, showAllMeasurables = false) {

    console.log({ctx});

    return prepareTabForCategory(
        ctx.measurableRatings,
        ctx.allocations,
        ctx.decommissions,
        ctx.primaryAssessments,
        application,
        showAllMeasurables);

}

export function determineStartingTab(categories = [], activeTab, lastViewedCategoryId) {
    // category last viewed or first with ratings, or simply first if no ratings
    const tabForLastCategoryViewed = _.find(categories, t => t.category.id === lastViewedCategoryId);
    const tabForActive = _.find(categories, t => t.category.id === activeTab?.category.id);
    return tabForActive || tabForLastCategoryViewed || categories[0];
}


export function determineEditableCategories(categories, permissions, userRoles) {
    const editableCategoriesForUser = _
        .chain(permissions)
        .filter(d => d.subjectKind === entity.MEASURABLE_RATING.key
            && _.includes(editOperations, d.operation)
            && d.qualifierReference.kind === entity.MEASURABLE_CATEGORY.key)
        .map(d => d.qualifierReference.id)
        .uniq()
        .value();

    return _.filter(
        categories,
        t => {
            const hasRole = _.includes(userRoles, t.ratingEditorRole);
            const editableCategoryForUser = _.includes(editableCategoriesForUser, t.id);
            const isEditableCategory = t.editable;
            return isEditableCategory && (hasRole || editableCategoryForUser);
        });
}