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
import namedSettings from "../../../system/named-settings";
import {entity} from "../../../common/services/enums/entity";
import {editOperations} from "../../../common/services/enums/operation";
import roles from "../../../user/system-roles";


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
        return loadAllData($q, serviceBroker, vm.parentEntityRef, false, force)
            .then((r) => {
                Object.assign(vm, r);
                vm.tabs = mkTabs(vm, false);
                const firstNonEmptyTab = determineStartingTab(vm.tabs);
                if (firstNonEmptyTab) {
                    vm.visibility.tab = firstNonEmptyTab.category.id;
                    vm.onTabChange(firstNonEmptyTab);
                }
            });
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
            vm.activeTab = _.find(vm.tabs, tab => tab.category.id === vm.activeAllocationScheme.measurableCategoryId);
        }
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
