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

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {hierarchyQueryScope} from "../../../common/services/enums/hierarchy-query-scope";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";

import template from "./person-overview.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    organisationalUnit: null
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
                CORE_API.PersonStore.getById,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.person = r.data;

                if(vm.person.organisationalUnitId) {
                    return serviceBroker
                        .loadAppData(
                            CORE_API.OrgUnitStore.getById,
                            [vm.person.organisationalUnitId])
                        .then(r => vm.organisationalUnit = Object.assign(
                            {},
                            r.data,
                            {kind: "ORG_UNIT"}));
                }
            });

        serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [ selector ])
            .then(r => vm.applications = r.data);

    };


    vm.$onInit = () => {
        loadAll();
    };

    vm.$onChanges = (changes) => {
        loadAll();
    };
}


controller.$inject = ["ServiceBroker"];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: "waltzPersonOverview"
};