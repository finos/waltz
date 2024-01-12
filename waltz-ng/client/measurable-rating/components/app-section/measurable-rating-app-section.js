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
import {determineEditableCategories, determineStartingTab, loadAllData, mkTab} from "../../measurable-rating-utils";
import namedSettings from "../../../system/named-settings";
import {selectedMeasurable} from "../panel/measurable-rating-panel-store";
import {mkSelectionOptions} from "../../../common/selector-utils";


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
        tab: null,
        loading: false
    },
    byCategory: {},
    activeAllocationScheme: null,
    roadmapsEnabled: false
};


function controller($q, serviceBroker, settingsService, userService) {
    const vm = initialiseData(this, initialState);

    function determineIfRoadmapsAreEnabled() {
        settingsService
            .findOrDefault(namedSettings.measurableRatingRoadmapsEnabled, true)
            .then(isEnabled => {
                vm.roadmapsEnabled = !(isEnabled === "false");
            });
    }

    const loadData = (force = false) => {
        if (vm.parentEntityRef){
            return loadAllData($q, serviceBroker, vm.parentEntityRef, force)
                .then(r => Object.assign(vm, r))
                .then(() => {
                    vm.tabs = _.filter(vm.categories, d => d.ratingCount > 0);

                    const startingTab = determineStartingTab(vm.tabs, vm.activeTab, vm.lastViewedCategoryId);
                    if (startingTab) {
                        vm.visibility.tab = startingTab.category.id;
                        vm.onTabChange(startingTab.category.id, force);
                    }
                });
        }
    };

    vm.$onInit = () => {

        determineIfRoadmapsAreEnabled();

        const permissionsPromise = serviceBroker
            .loadViewData(CORE_API.PermissionGroupStore.findForParentEntityRef, [vm.parentEntityRef])
            .then(r => r.data);

        const userRolePromise = userService
            .whoami()
            .then(user => vm.userRoles = user.roles);

        const allCategoriesPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => r.data);

        $q
            .all([permissionsPromise, userRolePromise, allCategoriesPromise])
            .then(([permissions, userRoles, allCategories]) => {
                const editableCategories = determineEditableCategories(allCategories, permissions, userRoles);
                vm.hasEditPermissions = !_.isEmpty(editableCategories);
            })
            .then(() => loadData());
    }


    // -- INTERACT ---

    const hideAllocationScheme = () => vm.activeAllocationScheme = null;

    vm.onShowAllocationScheme = (scheme) => {
        if (vm.activeAllocationScheme === scheme) {
            hideAllocationScheme();
        } else {
            vm.activeAllocationScheme = scheme;
        }
    };

    vm.onDismissAllocations = () => hideAllocationScheme();

    vm.onSaveAllocations = (changes) => {
        return serviceBroker
            .execute(
                CORE_API.AllocationStore.updateAllocations,
                [vm.parentEntityRef, vm.activeAllocationScheme.id, changes])
            .then(r => {
                loadData(true);
                return r.data;
            });
    };

    vm.onViewRatings = () => {
        vm.visibility.editor = false;
        loadData(true);
    };

    vm.onEditRatings = () => {
        vm.visibility.editor = true;
        loadData(true);
        hideAllocationScheme();
    };

    vm.onTabChange = (categoryId, force = false) => {
        vm.visibility.loading = true;
        selectedMeasurable.set(null);
        hideAllocationScheme();
        serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.getViewByCategoryAndAppSelector,
                [categoryId, mkSelectionOptions(vm.parentEntityRef)],
                {force})
            .then(r => {
                vm.activeTab = mkTab(r.data, vm.application);
                vm.visibility.loading = false;
            });
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "SettingsService",
    "UserService"
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
