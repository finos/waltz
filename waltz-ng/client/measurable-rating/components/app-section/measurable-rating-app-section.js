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
import {determineStartingTab, loadAllData, mkTab} from "../../measurable-rating-utils";
import namedSettings from "../../../system/named-settings";
import {entity} from "../../../common/services/enums/entity";
import {editOperations} from "../../../common/services/enums/operation";
import roles from "../../../user/system-roles";
import {selectedMeasurable} from "../panel/measurable-rating-panel-store";


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
            return loadAllData($q, serviceBroker, vm.parentEntityRef, false, force)
                .then(r => {
                    Object.assign(vm, r);
                    vm.tabs = vm.categories;
                    const startingTab = determineStartingTab(vm.tabs, vm.activeTab, vm.lastViewedCategoryId);
                    if (startingTab) {
                        vm.visibility.tab = startingTab.id;
                        vm.onTabChange(startingTab.id, force);
                    }
                });
        }
    };

    vm.$onInit = () => {
        determineIfRoadmapsAreEnabled();
        loadData()
            .then(() => serviceBroker.loadViewData(CORE_API.ApplicationStore.getById, [vm.parentEntityRef.id])
                .then(r => vm.application = r.data));

        const permissionsPromise = serviceBroker
            .loadViewData(CORE_API.PermissionGroupStore.findForParentEntityRef, [vm.parentEntityRef])
            .then(r => r.data);

        const involvementsPromise = serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findExistingInvolvementKindIdsForUser, [vm.parentEntityRef])
            .then(r => r.data);

        const userRolePromise = userService
            .whoami()
            .then(user => userService.hasRole(user, roles.RATING_EDITOR));
        $q
            .all([permissionsPromise, involvementsPromise, userRolePromise])
            .then(([permissions, involvements, hasEditRole]) => {

                vm.hasEditPermissions = !_
                    .chain(permissions)
                    .filter(d => d.subjectKind === entity.MEASURABLE_RATING.key && _.includes(editOperations, d.operation) && d.qualifierReference.kind === entity.MEASURABLE_CATEGORY.key)
                    .filter(d => d.requiredInvolvementsResult.areAllUsersAllowed || !_.isEmpty(_.intersection(d.requiredInvolvementsResult.requiredInvolvementKindIds, involvements)))
                    .isEmpty()
                    .value() || hasEditRole;
            });
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
            .then(r => loadData(true));
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
        selectedMeasurable.set(null);
        hideAllocationScheme();
        serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.getViewForEntityAndCategory,
                [vm.parentEntityRef, categoryId],
                {force})
            .then(r => {
                const tab = r.data;
                vm.activeTab = mkTab(tab);
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
