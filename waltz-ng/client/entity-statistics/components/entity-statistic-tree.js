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