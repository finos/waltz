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
import {buildHierarchies, findNode} from "../../../common/hierarchy-utils";
import template from "./entity-hierarchy-navigator.html";

const initialState = {
    model: [],
    expandedNodes: [],
    selectedNode: null,
    onSelect: (n) => console.log("No handler provided for entity-hierarchy-navigator:onSelect", n),
    treeOptions: {
        equality: (a, b) => a && b && a.id === b.id
    }
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
        if (changes.selectedNode && changes.selectedNode.currentValue && vm.forest) {
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
        onSelect: "<",
        expandedNodes: "<"
    },
    transclude: {
        "nodeTemplate": "?nodeTemplate"
    }
};


export default component;