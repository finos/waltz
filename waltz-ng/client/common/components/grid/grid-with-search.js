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
import {initialiseData, invokeFunction, termSearch} from "../../../common";
import template from "./grid-with-search.html";

const bindings = {
    columnDefs: "<",
    entries: "<",
    searchControlMinRows: "<?",
    searchPlaceholderText: "@?",
    scopeProvider: "<?",
    onInitialise: "<?",
    onChange: "<?",
    onRowSelect: "<?",
    localStorageKey: "@?"
};


const initialState = {
    columnDefs: [],
    entries: [],
    filteredEntries: [],
    scopeProvider: null,
    searchFields: [],
    searchControlMinRows: 5,
    searchPlaceholderText: "Search...",
    searchQuery: null,
    onInitialise: (gridApi) => {},
    onChange: (gridApi) => {}
};


function mkSearchFields(columnDefs = []) {
    return _
        .chain(columnDefs)
        .map(c => c.toSearchTerm || c.field)
        .compact()
        .value();
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        vm.filterEntries(vm.searchQuery);
        vm.searchFields = mkSearchFields(vm.columnDefs);
        invokeFunction(vm.onChange, { entriesCount: _.size(vm.filteredEntries) });
    };


    vm.filterEntries = query => {
        vm.searchQuery = query;
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
