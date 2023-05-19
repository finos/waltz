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
import moment from "moment";
import {CORE_API} from "../common/services/core-api-utils";
import {mkSelectionOptions} from "../common/selector-utils";
import {formats} from "../common";


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

    const allocationSchemesPromise = serviceBroker
        .loadAppData(CORE_API.AllocationSchemeStore.findAll)
        .then(r => ({allocationSchemes: r.data}));

    const ratingSchemesPromise = serviceBroker
        .loadAppData(CORE_API.RatingSchemeStore.findAll)
        .then(r => ({ratingSchemesById: _.keyBy(r.data, "id")}));

    const categoriesPromise = serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
        .then(r => ({
            categories: r.data,
            categoriesById: _.keyBy(r.data, d => d.id)
        }));

    const roadmapsPromise = serviceBroker
        .loadViewData(
            CORE_API.RoadmapStore.findRoadmapsAndScenariosByRatedEntity,
            [ parentEntityRef])
        .then(r => ({roadmapReferences: r.data}));

    // if we are in edit mode we will be loading all measurables, otherwise just the needed measurables
    const measurablesCall = allMeasurables
        ? serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll)
        : serviceBroker.loadViewData(
            CORE_API.MeasurableStore.findMeasurablesBySelector,
            [mkSelectionOptions(parentEntityRef)],
            { force });

    const measurablesPromise = measurablesCall
        .then(r => ({measurables: r.data}));

    const ratingsPromise = serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingStore.findForEntityReference,
            [ parentEntityRef ],
            { force })
        .then(r => ({ratings: r.data}));

    const allocationsPromise = serviceBroker
        .loadViewData(
            CORE_API.AllocationStore.findByEntity,
            [parentEntityRef],
            { force })
        .then(r => ({
            allocations: r.data,
            allocationTotalsByScheme: _
                .chain(r.data)
                .groupBy(d => d.schemeId)
                .mapValues(xs => _.sumBy(xs, x => x.percentage))
                .value()}));

    const decommPromise = loadDecommData(
        $q,
        serviceBroker,
        parentEntityRef,
        force);

    return $q
        .all([
            measurablesPromise,
            ratingSchemesPromise,
            ratingsPromise,
            categoriesPromise,
            allocationsPromise,
            allocationSchemesPromise,
            decommPromise,
            roadmapsPromise])
        .then(results => Object.assign({}, ...results));
}


/**
 *
 * @param ctx - {measurables: [], allocationSchemes: [], categories: [], ratingSchemesById: {}, allocations: []}
 * @param includeEmpty
 * @returns {*}
 */
export function mkTabs(ctx, includeEmpty = false) {

    const measurablesByCategory = _.groupBy(ctx.measurables, d => d.categoryId);
    const allocationSchemesByCategory = _.groupBy(ctx.allocationSchemes, d => d.measurableCategoryId);

    return _
        .chain(ctx.categories)
        .map(category => {
            const measurablesForCategory = measurablesByCategory[category.id] || [];
            const measurableIds = _.map(measurablesForCategory, d => d.id);
            const ratingsForCategory = _.filter(
                ctx.ratings,
                r => _.includes(measurableIds, r.measurableId));
            const ratingScheme = ctx.ratingSchemesById[category.ratingSchemeId];
            return {
                category,
                ratingScheme,
                measurables: measurablesForCategory,
                ratings: ratingsForCategory,
                allocationSchemes: allocationSchemesByCategory[category.id] || [],
                allocations: ctx.allocations
            };
        })
        .filter(t => t.measurables.length > 0 || includeEmpty)
        .orderBy([tab => tab.category.position, tab => tab.category.name])
        .value();
}


export function determineStartingTab(tabs = []) {
    // first with ratings, or simply first if no ratings
    return _.find(tabs, t => t.ratings.length > 0 ) || tabs[0];
}

