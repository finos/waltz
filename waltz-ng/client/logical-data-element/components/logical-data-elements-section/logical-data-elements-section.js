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
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import template from "./logical-data-elements-section.html";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    dataTypes: [],
    dataElements: []
};


function prepareData(dataTypes = [], dataElements = []) {
    const typesById = _.keyBy(dataTypes, "id");

    return _.chain(dataElements)
        .groupBy("parentDataTypeId")
        .map((ldes, typeId) => {
            return {
                dataType: typesById[typeId],
                dataElementRefs: _.sortBy(ldes, "name")
            }
        })
        .value();
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const dtPromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);

        const ldePromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalDataElementStore.findBySelector,
                [ mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.dataElements = r.data);

        dtPromise
            .then(() => ldePromise)
            .then(() => vm.groupedDataElements = prepareData(vm.dataTypes, vm.dataElements));
    };

    vm.$onChanges = () => {
        vm.groupedDataElements = prepareData(vm.dataTypes, vm.dataElements);
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


const id = "waltzLogicalDataElementsSection";


export default {
    component,
    id
};
