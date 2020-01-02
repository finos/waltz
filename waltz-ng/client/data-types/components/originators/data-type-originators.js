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

import {initialiseData} from "../../../common"
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./data-type-originators.html";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    dataTypes: [],
    flowOriginators: {}
};


function prepareData(dataTypes = [], flowOriginators = {}) {
    const typesById = _.keyBy(dataTypes, "id");

    // TODO: enrich with dtu flow info
    return _.map(flowOriginators, (apps, typeId) => {
        return {
            dataType: typesById[typeId],
            appReferences: _.sortBy(apps, "name")
        };
    });


}


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        const dtPromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);

        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        const originatorPromise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.findForUsageKindByDataTypeIdSelector,
                [ "ORIGINATOR", selector ])
            .then(r => vm.flowOriginators = r.data);

        dtPromise
            .then(() => originatorPromise)
            .then(() => vm.originators = prepareData(vm.dataTypes, vm.flowOriginators));
    };


    vm.$onInit = () => {
        loadAll();
    };


    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadAll();
        }
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzDataTypeOriginators";


export default {
    component,
    id
};
