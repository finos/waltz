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


export function loadAllData(
    $q,
    serviceBroker,
    vm,
    allMeasurables = false,
    force = false) {

    serviceBroker
        .loadViewData(
            CORE_API.RoadmapStore.findRoadmapsAndScenariosByRatedEntity,
            [ vm.parentEntityRef ])
        .then(r => vm.roadmapReferences = r.data);

    const ratingsPromise = serviceBroker
        .loadViewData(CORE_API.MeasurableRatingStore.findForEntityReference, [ vm.parentEntityRef ], { force })
        .then(r => vm.ratings = r.data);

    const ratingSchemesPromise = serviceBroker
        .loadAppData(CORE_API.RatingSchemeStore.findAll)
        .then(r => vm.ratingSchemesById = _.keyBy(r.data, "id"));

    const categoriesPromise = serviceBroker
        .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
        .then(r => vm.categories = r.data);

    const measurablesPromise = serviceBroker
        .loadViewData(
            allMeasurables
                ? CORE_API.MeasurableStore.findAll
                : CORE_API.MeasurableStore.findMeasurablesRelatedToPath,
            [vm.parentEntityRef],
            { force })
        .then(r => vm.measurables = r.data);

    const allocationSchemesPromise = serviceBroker
        .loadViewData(CORE_API.AllocationSchemeStore.findAll)
        .then(r => vm.allocationSchemes = r.data);

    const allocationsPromise = serviceBroker
        .loadViewData(
            CORE_API.AllocationStore.findByEntity,
            [vm.parentEntityRef],
            { force: true})
        .then(r => vm.allocations = r.data)
        .then(() => vm.allocationTotalsByScheme = _
            .chain(vm.allocations)
            .groupBy(d => d.schemeId)
            .mapValues(xs => _.sumBy(xs, x => x.percentage))
            .value());

    const replacementAppPromise = serviceBroker
        .loadViewData(CORE_API.MeasurableRatingReplacementStore.findForEntityRef, [vm.parentEntityRef])
        .then(r => vm.replacementApps = r.data);

    const decommissionDatePromise = serviceBroker
        .loadViewData(CORE_API.MeasurableRatingPlannedDecommissionStore.findForEntityRef, [vm.parentEntityRef])
        .then(r => vm.plannedDecommissions = r.data);

    return $q
        .all([
            measurablesPromise,
            ratingSchemesPromise,
            ratingsPromise,
            categoriesPromise,
            allocationsPromise,
            allocationSchemesPromise])
        .then(() => {
            vm.tabs = mkTabs(
                vm.categories,
                vm.ratingSchemesById,
                vm.measurables,
                vm.ratings,
                vm.allocationSchemes,
                vm.allocations,
                false /*include empty */);
        });
}


export function mkTabs(categories = [],
                       ratingSchemesById = {},
                       measurables = [],
                       ratings = [],
                       allocationSchemes = [],
                       allocations = [],
                       includeEmpty = true) {

    const measurablesByCategory = _.groupBy(measurables, d => d.categoryId);
    const allocationSchemesByCategory = _.groupBy(allocationSchemes, d => d.measurableCategoryId);

    return _.chain(categories)
        .map(category => {
            const measurablesForCategory = measurablesByCategory[category.id] || [];
            const measurableIds = _.map(measurablesForCategory, "id");
            const ratingsForCategory = _.filter(
                ratings,
                r => _.includes(measurableIds, r.measurableId));
            const ratingScheme = ratingSchemesById[category.ratingSchemeId];
            return {
                category,
                ratingScheme,
                measurables: measurablesForCategory,
                ratings: ratingsForCategory,
                allocationSchemes: allocationSchemesByCategory[category.id] || [],
                allocations
            };
        })
        .filter(t => t.ratings.length > 0 || includeEmpty)
        .sortBy(tab => tab.category.name)
        .value();
}


export function determineStartingTab(tabs = []) {
    // first with ratings, or simply first if no ratings
    return _.find(tabs, t => t.ratings.length > 0 ) || tabs[0];
}


