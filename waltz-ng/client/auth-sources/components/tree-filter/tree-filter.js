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

import template from "./tree-filter.html";
import {initialiseData} from "../../../common/index";
import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../common/hierarchy-utils";
import _ from "lodash";

const bindings = {
    onFilterChange: "<",
    items: "<",
    placeholder: "@?"
};

const initialState = {
    placeholder: "Search...",
    editMode: false,
    selectedItems: [],
};


function prepareTree(items = []) {
    return buildHierarchies(items, false);
}

function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const items = _.map(vm.items, d => {
            const concrete = _.isUndefined(d.concrete) ? true : d.concrete;
            return Object.assign({}, d, {concrete})
        });
        vm.searchNodes = prepareSearchNodes(items);
        vm.hierarchy = prepareTree(items);
    };

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.doSearch = (termStr = "") => {
        const matchingNodes = doSearch(termStr, vm.searchNodes);
        vm.hierarchy = prepareTree(matchingNodes);
    };

    vm.toggleTreeSelector = () => {
        vm.editMode = !vm.editMode;
    };

    const sortByName = (items = []) => items.sort((a, b) => {
        if (a.name < b.name) {
            return -1;
        } else if (a.name > b.name) {
            return 1;
        } else {
            return 0
        }
    });

    vm.onSelect = (n) => {
        if (!vm.selectedItems.map(e => e.id).includes(n.id)) {
            let newSelectedItems = [...vm.selectedItems];
            newSelectedItems.push(n);
            vm.selectedItems = sortByName(newSelectedItems);
        }
        notifyListeners(vm.selectedItems);
    };

    const notifyListeners = (selectedItems) => {
        const findChildren = (node, acc = []) => {
            acc.push(node);
            node.children.forEach(c => findChildren(c, acc));
            return acc;
        };
        const expandedSelection = _.chain(selectedItems).flatMap(d => findChildren(d)).map(d => d.id).uniq().value();
        vm.onFilterChange(expandedSelection);
    };

    vm.removeSelected = (id) => {
        vm.selectedItems = vm.selectedItems.filter(e => e.id !== id);
        notifyListeners(vm.selectedItems);
    };

    vm.isSelected = (n) => {
        return vm.selectedItems.map(e => e.id).includes(n.id)
    };
}


export const component = {
    controller,
    bindings,
    template
};


export const id = "waltzTreeFilter";
