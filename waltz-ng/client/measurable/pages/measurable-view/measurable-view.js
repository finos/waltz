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
import {initialiseData} from "../../../common";

import template from "./measurable-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";


const initialState = {
    filters: {},
};


function logHistory(measurable, historyStore) {
    return historyStore
        .put(measurable.name,
            "MEASURABLE",
            "main.measurable.view",
            { id: measurable.id });
}


function controller($q,
                    $stateParams,
                    serviceBroker,
                    historyStore) {

    const id = $stateParams.id;
    const ref = { id, kind: "MEASURABLE" };
    const childrenSelector = { entityReference: ref, scope: "CHILDREN" };

    const vm = initialiseData(this, initialState);
    vm.entityReference = ref;
    vm.selector = childrenSelector;
    vm.scope = childrenSelector.scope;


    // -- BOOT ---

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.MeasurableStore.getById, [ id ])
            .then(r => {
                vm.measurable = r.data;
                vm.entityReference = toEntityRef(vm.measurable);
            })
            .then(() => serviceBroker
                .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
                .then(r => vm.measurableCategory = _.find(r.data, { id: vm.measurable.categoryId })))
            .then(() => logHistory(vm.measurable, historyStore));
    };


    vm.filtersChanged = (filters) => {
        vm.filters = filters;
    };

}


controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker",
    "HistoryStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
