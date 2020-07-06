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
import template from './app-group-list.html';
import {initialiseData, termSearch} from "../../../common";


const bindings = {
    groupSubscriptions: '<',
    unsubscribe: '<',
    deleteGroup: '<'
};


const initialData = {
    searchTerms: "",
    filteredSubscriptions: []
};


function controller() {
    const vm = initialiseData(this, initialData);

    vm.$onChanges = () => vm.searchTermsChanged(vm.searchTerms);

    vm.clearSearch = () => vm.searchTermsChanged("");

    vm.searchTermsChanged = (terms) => {
        vm.searchTerms = terms;
        vm.filteredSubscriptions = termSearch(
            vm.groupSubscriptions,
            terms,
            ["appGroup.name"]);
    };
}

controller.$inject = [];

const component = {
    controller,
    template,
    bindings
}

export default {
    component,
    id: "waltzAppGroupList"
}