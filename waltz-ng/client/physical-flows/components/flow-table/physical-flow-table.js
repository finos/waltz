/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {initialiseData} from "../../../common";

import template from "./physical-flow-table.html";
import {withWidth, columnDef, fetchData} from "../../../physical-flow/physical-flow-table-utilities";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: "<",
    optionalColumnDefs: "<"
};


const initialState = {
    tableData: []
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const defaultColumnDefs = [
        withWidth(columnDef.name, "20%"),
        columnDef.extId,
        columnDef.observation,
        columnDef.format,
        columnDef.transport,
        columnDef.frequency,
        columnDef.criticality,
        columnDef.description
    ];

    vm.columnDefs = vm.optionalColumnDefs == null
        ? defaultColumnDefs
        : vm.optionalColumnDefs;

    vm.$onInit = () => {
        vm.tableData = fetchData(vm.parentEntityRef, $q, serviceBroker)
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