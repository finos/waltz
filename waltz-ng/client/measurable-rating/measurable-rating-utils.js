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
    allMeasurables = false,
    force = false) {


    const ratingsPromise2 = serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingStore.getViewForEntityAndCategory,
            [ parentEntityRef, 33 ],
            { force })
        .then(r => {

            const view = r.data;
            console.log({view});
        });

    const ratingsPromise = serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingStore.getViewForEntity,
            [ parentEntityRef ],
            { force })
        .then(r => {

            const view = r.data;

            const allocationTotalsByScheme =  _
                .chain(view.allocations)
                .groupBy(d => d.schemeId)
                .mapValues(xs => _.sumBy(xs, x => x.percentage))
                .value();

            const ratingSchemeItemsById =  _
                .chain(view.ratingSchemes)
                .flatMap(d => d.ratings)
                .keyBy(d => d.id)
                .value();

            return Object.assign(view, {
                allocationTotalsByScheme,
                categoriesById: _.keyBy(view.categories, d => d.id),
                ratingSchemesById: _.keyBy(view.ratingSchemes, d => d.id),
                ratings: view.measurableRatings,
                ratingSchemeItemsById
            });
        });

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
            ratingsPromise,
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
                               ratingSchemesById,
                               ratingSchemeItemsById,
                               showAllMeasurables = true) {

    const ratedMeasurables = _.map(ratingsForCategory, d => d.measurableId);
    const ratingScheme = ratingSchemesById[category.ratingSchemeId];
    const ratingSchemeItemsByCode = _.keyBy(ratingScheme.ratings, d => d.rating);

    const measurables = showAllMeasurables
        ? measurablesForCategory
        : reduceToSelectedNodesOnly(measurablesForCategory, ratedMeasurables);

    const ratings = _
        .chain(ratingsForCategory)
        .map(r => Object.assign(r, {ratingSchemeItem: ratingSchemeItemsByCode[r.rating]}))
        .value();

    const assessmentRatings = _
        .chain(assessmentRatingsForCategory)
        .map(r => Object.assign(r, {ratingSchemeItem: ratingSchemeItemsById[r.ratingId]}))
        .value();

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
        plannedReplacements: replacementsForCategory
    };
}

/**
 *
 * @param ctx - {measurables: [], allocationSchemes: [], categories: [], ratingSchemesById: {}, allocations: [], ratings: []}
 * @param includeEmpty
 * @returns {*}
 */
export function mkTabs(ctx, includeEmpty = false, showAllMeasurables = false) {

    const measurablesByCategory = _.groupBy(ctx.measurables, d => d.categoryId);
    const allocationSchemesByCategory = _.groupBy(ctx.allocationSchemes, d => d.measurableCategoryId);
    const ratingSchemesById = _.keyBy(ctx.ratingSchemes, d => d.id);

    return _
        .chain(ctx.categories)
        .map(category => {

            const measurablesForCategory = measurablesByCategory[category.id] || [];
            const measurableIds = _.map(measurablesForCategory, d => d.id);
            console.log({measurablesForCategory});


            const ratingsForCategory = _.filter(ctx.ratings, d => _.includes(measurableIds, d.measurableId));
            const ratingIds = _.map(ratingsForCategory, d => d.id);

            const definitionsForCategory = _.filter(
                ctx.assessmentDefinitions,
                r => r.qualifierReference.id === category.id);

            const decommsForCategory = _.filter(
                ctx.plannedDecommissions || [],
                r => _.includes(ratingIds, r.measurableRatingId));

            const decommIds = _.map(decommsForCategory, d => d.id);

            const replacementsForCategory = _.filter(
                ctx.plannedReplacements || [],
                r => _.includes(decommIds, r.decommissionId));

            const allocationSchemesForCategory = _.get(allocationSchemesByCategory, [category.id], []);

            const assessmentRatingsForCategory = _
                .chain(ctx.assessmentRatings || [])
                .filter(r => _.includes(ratingIds, r.entityReference.id))
                .value();

            return prepareTabForCategory(
                category,
                ratingsForCategory,
                measurablesForCategory,
                allocationSchemesForCategory,
                ctx.allocations,
                definitionsForCategory,
                assessmentRatingsForCategory,
                decommsForCategory,
                replacementsForCategory,
                ratingSchemesById,
                ctx.ratingSchemeItemsById,
                showAllMeasurables);

        })
        .filter(t => t.measurables.length > 0 || includeEmpty)
        .orderBy([tab => tab.category.position, tab => tab.category.name])
        .value();
}

export function mkTab(ctx, includeEmpty = false, showAllMeasurables = false) {

    console.log(ctx);

    // return prepareTabForCategory(
    //     ctx.category,
    //     ratingsForCategory,
    //     measurablesForCategory,
    //     allocationSchemesForCategory,
    //     ctx.allocations,
    //     definitionsForCategory,
    //     assessmentRatingsForCategory,
    //     decommsForCategory,
    //     replacementsForCategory,
    //     ratingSchemesById,
    //     ctx.ratingSchemeItemsById,
    //     showAllMeasurables);

}


export function determineStartingTab(tabs = [], lastViewedCategoryId) {
    // category last viewed or first with ratings, or simply first if no ratings
    const tabForLastCategoryViewed = _.find(tabs, t => t.category.id === lastViewedCategoryId);
    const firstTabWithRatings = _.find(tabs, t => t.ratings.length > 0);
    return tabForLastCategoryViewed || firstTabWithRatings || tabs[0];
}

