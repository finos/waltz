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

import template from "./unit-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";


const initialState = {
    filters: {},
    parentEntityRef: {},
};


const addToHistory = (historyStore, orgUnit) => {
    if (! orgUnit) { return; }
    historyStore.put(
        orgUnit.name,
        'ORG_UNIT',
        'main.org-unit.view',
        { id: orgUnit.id });
};


function controller($stateParams,
                    dynamicSectionManager,
                    historyStore,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const id = $stateParams.id;
        vm.parentEntityRef = { kind: "ORG_UNIT", id };

        dynamicSectionManager.initialise("ORG_UNIT");
        serviceBroker
            .loadViewData(CORE_API.OrgUnitStore.getById, [ id ])
            .then(r => vm.orgUnit = r.data)
            .then(() => addToHistory(historyStore, vm.orgUnit));

    };


    vm.filtersChanged = (filters) => {
        vm.filters = filters;
    };

    // -- DYNAMIC SECTIONS

}


controller.$inject = [
    '$stateParams',
    'DynamicSectionManager',
    'HistoryStore',
    'ServiceBroker'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
