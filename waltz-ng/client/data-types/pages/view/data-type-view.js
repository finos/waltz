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

import {initialiseData} from "../../../common";

import template from "./data-type-view.html";
import {toEntityRef} from "../../../common/entity-utils";


const initialState = {
    dataType: null,
    entityRef: null,
    filters: {}
};


function controller(dataType,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    if(dataType) {
        vm.entityRef = toEntityRef(dataType);
        vm.dataType = dataType;

        // -- BOOT ---
        vm.$onInit = () => {
            historyStore.put(
                dataType.name,
                "DATA_TYPE",
                "main.data-type.view",
                {id: dataType.id});
        };
    }

    // -- INTERACT --
    vm.filtersChanged = (filters) => {
        vm.filters = filters;
    };
}


controller.$inject = [
    "dataType",
    "HistoryStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};