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
import {getParents, populateParents, switchToParentIds} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from "./org-unit-overview.html";
import {initialiseData} from "../../../common/index";
import {mkSelectionOptions} from '../../../common/selector-utils';
import {hierarchyQueryScope} from '../../../common/services/enums/hierarchy-query-scope';
import {entityLifecycleStatus} from '../../../common/services/enums/entity-lifecycle-status';


const bindings = {
    filters: "<",
    parentEntityRef: "<",
};


const initialState = {
    visibility: {
        childDisplayMode: "LIST"
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            hierarchyQueryScope.CHILDREN.key,
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector] )
            .then(r => vm.apps = r.data);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.calculateStats,
                [ selector ])
            .then(r => vm.flowStats = r.data);

        serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => {
                const orgUnits = populateParents(r.data, true);
                vm.orgUnit = _.find(orgUnits, { id: vm.parentEntityRef.id });
                vm.parentOrgUnits = _.reverse(getParents(vm.orgUnit));
                vm.childOrgUnits = _.get(vm, "orgUnit.children", []);
                vm.descendantOrgUnitTree = switchToParentIds([ vm.orgUnit ]);
            });
    };

    vm.$onInit = () => {

    };


    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadAll();
        }
    };
}


controller.$inject = ["ServiceBroker"];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzOrgUnitOverview"
};
