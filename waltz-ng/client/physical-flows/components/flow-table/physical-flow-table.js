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

import template from "./physical-flow-table.html";
import {columnDef, fetchData, withWidth} from "../../../physical-flow/physical-flow-table-utilities";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: "<",
    optionalColumnDefs: "<?"
};


const initialState = {
    tableData: [],
    optionalColumnDefs: []
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const defaultColumnDefs = [
        withWidth(columnDef.name, "20%"),
        withWidth(columnDef.specName, "20%"),
        columnDef.extId,
        columnDef.format,
        columnDef.transport,
        columnDef.frequency,
        columnDef.criticality,
        columnDef.description
    ];

    vm.$onChanges = () => {

        vm.columnDefs = _.isEmpty(vm.optionalColumnDefs)
            ? defaultColumnDefs
            : vm.optionalColumnDefs;

        fetchData(vm.parentEntityRef, $q, serviceBroker)
            .then(data => vm.tableData = data);

        vm.selectorOptions = mkSelectionOptions(
            vm.parentEntityRef,
            "EXACT");
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker",
];


const component = {
    bindings,
    template,
    controller
};


export default component;