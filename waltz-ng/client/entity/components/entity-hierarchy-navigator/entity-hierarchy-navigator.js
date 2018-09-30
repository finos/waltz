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
import {buildHierarchies, findNode} from "../../../common/hierarchy-utils";
import template from "./entity-hierarchy-navigator.html";

const initialState = {
    model: [],
    expandedNodes: [],
    selectedNode: null,
    onSelect: (n) => console.log("No handler provided for entity-hierarchy-navigator:onSelect", n)
};


function recalcHierarchy(model = []) {
    return buildHierarchies(model, false);
}


function findParents(forest, currentNode) {
    let parents = [];
    let ptr = currentNode;
    while (ptr.parent) {
        ptr = findNode(forest, ptr.parent);
        parents.push(ptr);
    }
    return parents;
}


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = ((changes) => {
        if (changes.model && changes.model.currentValue) {
            vm.forest = recalcHierarchy(vm.model);
        }
        if (changes.selectedNode && changes.selectedNode.currentValue) {
            vm.currentNode = findNode(vm.forest, vm.selectedNode.id);
            vm.expandedNodes = findParents(vm.forest, vm.currentNode);
        }
    });

    vm.handleSelection = (node) =>  { if (node !== vm.selectedNode) vm.onSelect(node); }
}


const component = {
    template,
    controller,
    bindings: {
        selectedNode: "<",
        model: "<",
        onSelect: "<"
    },
    transclude: {
        "nodeTemplate": "?nodeTemplate"
    }
};


export default component;