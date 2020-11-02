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
import {determineStartingTab, loadAllData, mkTabs} from "../../measurable-rating-utils";


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

    const loadData = (force = false) => {
        return loadAllData($q, serviceBroker, vm.parentEntityRef, false, force)
            .then((r) => {
                Object.assign(vm, r);
                vm.tabs = mkTabs(vm, false);
                const firstNonEmptyTab = determineStartingTab(vm.tabs);
                vm.visibility.tab = firstNonEmptyTab ? firstNonEmptyTab.category.id : null;
                vm.onTabChange(firstNonEmptyTab);
            });
    };

    vm.$onInit = () => loadData()
        .then(() => serviceBroker.loadViewData(CORE_API.ApplicationStore.getById, [vm.parentEntityRef.id])
            .then(r => vm.application = r.data));


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
            .then(r => {
                loadAllData($q, serviceBroker, vm.parentEntityRef, false, true)
                    .then(r => {
                        Object.assign(vm, r);
                        mkTabs(vm);
                    });
                return r.data;
            });
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

        serviceBroker
            .loadViewData(
                CORE_API.RatingSchemeStore.findRatingsForEntityAndMeasurableCategory,
                [vm.parentEntityRef, tab.category.id])
            .then(r => tab.ratingSchemeItems = r.data)
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
