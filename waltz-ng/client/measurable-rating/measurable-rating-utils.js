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


export function loadDecommData(
    $q,
    serviceBroker,
    parentEntityRef,
    force = false) {

    const replacementAppPromise = serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingReplacementStore.findForEntityRef,
            [parentEntityRef],
            {force})
        .then(r => ({replacementApps: r.data}));

    const decommissionDatePromise = serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingPlannedDecommissionStore.findForEntityRef,
            [parentEntityRef],
            {force})
        .then(r => r.data);

    const replacingDecomms = serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingPlannedDecommissionStore.findForReplacingEntityRef,
            [parentEntityRef])
        .then(r => ({replacingDecommissions: r.data}));

    const parentApplication = serviceBroker.loadViewData(CORE_API.ApplicationStore.getById, [parentEntityRef.id])
        .then(r => r.data);

    return $q
        .all([replacementAppPromise, decommissionDatePromise, replacingDecomms, parentApplication])
        .then(([replacementApps, decommissionDates, replacingDecoms, parentApplication]) => {

            const appRetirementDate = new Date(parentApplication.plannedRetirementDate);

            const plannedDecomms = (_.isNull(parentApplication.plannedRetirementDate))
                ? _.map(decommissionDates, d => Object.assign({}, d, { isValid: true}))
                : _.map(decommissionDates,d => {

                    const decomDate = new Date(d.plannedDecommissionDate);

                    const sameDate = appRetirementDate.getFullYear() === decomDate.getFullYear()
                        && appRetirementDate.getMonth() === decomDate.getMonth()
                        && appRetirementDate.getDate() === decomDate.getDate();

                    const isValid = appRetirementDate > decomDate || sameDate;

                    return Object.assign({}, d, { isValid: isValid})
                });

            return Object.assign({},
                                 replacementApps,
                                 replacingDecoms,
                                 {plannedDecommissions: plannedDecomms});
        });
}





export function loadAllData(
    $q,
    serviceBroker,
    parentEntityRef,
    force = false) {

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
            categoriesPromise,
            lastViewedCategoryPromise
        ])
        .then(results => Object.assign({}, ...results));
}


function prepareTabForCategory(category,
                               ratingsForCategory = [],
                               measurablesForCategory = [],
                               allocationSchemesForCategory = [],
                               allocationsForCategory = [],
                               assessmentDefinitionsForCategory = [],
                               assessmentRatingsForCategory = [],
                               decommsForCategory = [],
                               replacementsForCategory = [],
                               replacingDecommissionsForCategory = [],
                               ratingSchemesById,
                               ratingSchemeItemsById,
                               showAllMeasurables = true) {

    const ratedMeasurables = _.map(ratingsForCategory, d => d.measurableId);
    const replacingMeasurables = _.map(replacingDecommissionsForCategory, d => d.measurableRating.measurableId);

    const requiredMeasurables = _.concat(ratedMeasurables, replacingMeasurables);

    const ratingScheme = ratingSchemesById[category.ratingSchemeId];
    const ratingSchemeItemsByCode = _.keyBy(ratingScheme.ratings, d => d.rating);

    const measurables = showAllMeasurables
        ? measurablesForCategory
        : reduceToSelectedNodesOnly(measurablesForCategory, requiredMeasurables);

    const ratings = _
        .chain(ratingsForCategory)
        .map(r => Object.assign(r, {ratingSchemeItem: ratingSchemeItemsByCode[r.rating]}))
        .value();

    const assessmentRatings = _
        .chain(assessmentRatingsForCategory)
        .map(r => Object.assign(r, {ratingSchemeItem: ratingSchemeItemsById[r.ratingId]}))
        .value();

    const allocationTotalsByScheme = _
        .chain(allocationsForCategory)
        .groupBy(d => d.schemeId)
        .mapValues(d => _.sumBy(d, a => a.percentage))
        .value()

    return {
        category,
        ratingScheme,
        measurables,
        ratings,
        allocationSchemes: allocationSchemesForCategory,
        allocations: allocationsForCategory,
        assessmentDefinitions: assessmentDefinitionsForCategory,
        assessmentRatings,
        plannedDecommissions: decommsForCategory,
        plannedReplacements: replacementsForCategory,
        replacingDecommissions: replacingDecommissionsForCategory,
        ratingSchemeItems: ratingScheme.ratings,
        allocationTotalsByScheme
    };
}

export function mkTab(ctx, showAllMeasurables = false) {

    const ratingSchemesById = _.keyBy(ctx.ratingSchemes, d => d.id);

    return prepareTabForCategory(
        ctx.category,
        ctx.ratings,
        ctx.measurables,
        ctx.allocationSchemes,
        ctx.allocations,
        ctx.assessmentDefinitions,
        ctx.assessmentRatings,
        ctx.plannedDecommissions,
        ctx.plannedReplacements,
        ctx.replacingDecommissions,
        ratingSchemesById,
        ctx.ratingSchemeItemsById,
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