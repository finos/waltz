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

import {buildHierarchies, findNode} from "../../common/hierarchy-utils";
import template from "./entity-statistic-tree.html";


const bindings = {
    definitions: "<",
    onSelection: "<",
    currentDefinitionId: "<"
};


const initialState = {
    expandedNodes: [],
    currentNode: null
};


function buildDefinitionTree(definitions = []) {
    return buildHierarchies(definitions, false);
}


function findParents(forest, currentNode) {
    let parents = [];
    let ptr = currentNode;
    while (ptr && ptr.parent) {
        ptr = findNode(forest, ptr.parent);
        parents.push(ptr);
    }
    return parents;
}


function controller() {
    const vm = Object.assign(this, initialState);

    vm.$onChanges = () => {
        vm.definitionTree = buildDefinitionTree(vm.definitions);
        vm.currentNode = findNode(vm.definitionTree, vm.currentDefinitionId);
        vm.expandedNodes = findParents(vm.definitionTree, vm.currentNode);
    };

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.handleSelection = (node) =>  {
        if (node.id !== vm.currentDefinitionId) {
            vm.currentDefinitionId = node.id;
            vm.onSelection(node);
        }
    }
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;