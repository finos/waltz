/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

import _ from "lodash";
import {initialiseData, invokeFunction, termSearch} from "../../../common";
import template from "./grid-with-search.html";

const bindings = {
    columnDefs: "<",
    entries: "<",
    searchPlaceholderText: "@",
    scopeProvider: "<?",
    onInitialise: "<",
    onChange: "<",
    onRowSelect: "<?"
};


const initialState = {
    columnDefs: [],
    entries: [],
    filteredEntries: [],
    scopeProvider: null,
    searchFields: [],
    searchPlaceholderText: "Search...",
    onInitialise: (gridApi) => console.log("Default onInitialise handler for grid-search: ", gridApi),
    onChange: (gridApi) => {}
};


function mkSearchFields(columnDefs = []) {
    return _.chain(columnDefs)
        .filter(c => !_.isUndefined(c.field))
        .map("field")
        .value();
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filteredEntries = vm.entries;
        vm.searchFields = mkSearchFields(vm.columnDefs);
        invokeFunction(vm.onChange, { entriesCount: _.size(vm.filteredEntries) });
    };


    vm.filterEntries = query => {
        vm.filteredEntries = termSearch(vm.entries, query, vm.searchFields);
        invokeFunction(vm.onChange, { entriesCount: _.size(vm.filteredEntries) });
    };

}


const component = {
    bindings,
    template,
    controller
};


export default component;
