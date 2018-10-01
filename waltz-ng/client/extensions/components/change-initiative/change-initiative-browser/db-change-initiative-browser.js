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
