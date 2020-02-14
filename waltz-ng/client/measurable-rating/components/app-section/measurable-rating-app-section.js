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
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";

import template from "./measurable-rating-app-section.html";
import {determineStartingTab, mkTabs} from "../../measurable-rating-utils";


/**
 * @name waltz-measurable-rating-panel
 *
 * @description
 * This component render multiple <code>measurable-rating-panel</code> components
 * within a tab group.
 *
 * It is intended to be used to show measurables and ratings for a single entity (app or actor).
 */

const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    categories: [],
    ratings: [],
    measurables: [],
    visibility: {
        editor: false,
        overlay: false,
        tab: null
    },
    byCategory: {},
    activeAllocationScheme: null
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadAllocations() {
        const allocationsPromise = serviceBroker
            .loadViewData(
                CORE_API.AllocationStore.findByEntity,
                [vm.parentEntityRef],
                { force: true})
            .then(r => vm.allocations = r.data);

        return allocationsPromise
            .then(() => vm.allocationTotalsByScheme = _
                .chain(vm.allocations)
                .groupBy(d => d.schemeId)
                .mapValues(xs => _.sumBy(xs, x => x.percentage))
                .value());
    }

    const loadData = (force = false) => {
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
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesRelatedToPath, [vm.parentEntityRef], { force })
            .then(r => vm.measurables = r.data);

        const allocationSchemesPromise = serviceBroker
            .loadViewData(CORE_API.AllocationSchemeStore.findAll)
            .then(r => vm.allocationSchemes = r.data);

        const allocationsPromise = loadAllocations();

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
                const firstNonEmptyTab = determineStartingTab(vm.tabs);
                vm.visibility.tab = firstNonEmptyTab ? firstNonEmptyTab.category.id : null;
            });
    };

    vm.$onInit = () => loadData();


    // -- INTERACT ---

    const hideAllocationScheme = () => vm.activeAllocationScheme = null;

    vm.onShowAllocationScheme = (scheme) => {
        if (vm.activeAllocationScheme === scheme) {
            hideAllocationScheme();
        } else {
            vm.activeAllocationScheme = scheme;
        }

        vm.activeTab = _.find(vm.tabs, tab => tab.category.id === vm.activeAllocationScheme.measurableCategoryId);
    };

    vm.onDismissAllocations = () => hideAllocationScheme();

    vm.onSaveAllocations = (changes) => {
        return serviceBroker
            .execute(
                CORE_API.AllocationStore.updateAllocations,
                [vm.parentEntityRef, vm.activeAllocationScheme.id, changes])
            .then(r => { loadAllocations(); return r; });
    };

    vm.onViewRatings = () => {
        vm.visibility.editor = false;
        loadData(true);
    };

    vm.onEditRatings = () => {
        vm.visibility.editor = true;
        hideAllocationScheme();
    };

    vm.onTabChange = (tab) => {
        hideAllocationScheme();
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};



export default {
    component,
    id: "waltzMeasurableRatingAppSection"
};
