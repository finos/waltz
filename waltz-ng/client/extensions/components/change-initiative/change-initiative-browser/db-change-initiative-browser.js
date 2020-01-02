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

import {initialiseData} from "../../../../common";

import {buildHierarchies} from "../../../../common/hierarchy-utils";

import template from "./db-change-initiative-browser.html";


const bindings = {
    changeInitiatives: "<",
    scrollHeight: "<",
    onSelect: "<"
};


const initialState = {
    containerClass: [],
    changeInitiatives: [],
    treeData: [],
    visibility: {
        sourcesOverlay: false
    },
    onSelect: (d) => console.log("wcib: default on-select", d),
};


function prepareTreeData(data = []) {
    return buildHierarchies(data, false);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.changeInitiatives) {
            vm.treeData = prepareTreeData(vm.changeInitiatives);
        }

        if (vm.scrollHeight && vm.treeData && vm.treeData.length > 10) {
            vm.containerClass = [
                `waltz-scroll-region-${vm.scrollHeight}`
            ];
        }
    }
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzDbChangeInitiativeBrowser"
};
