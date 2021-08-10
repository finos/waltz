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

import template from './tree-picker.html';
import {initialiseData} from "../../../common/index";
import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../common/hierarchy-utils";
import _ from "lodash";

const bindings = {
    onSelect: '<',
    items: '<',
    placeholder: '@?'
};

const initialState = {
    placeholder: 'Search...'
};


function prepareTree(items = []) {
    return buildHierarchies(items, false);
}


function prepareExpandedNodes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const items = _.map(vm.items, d => {
            const concrete =  _.isUndefined(d.concrete) ? true : d.concrete;
            return Object.assign({}, d, { concrete })
        });
        vm.searchNodes = prepareSearchNodes(items);
        vm.hierarchy = prepareTree(items);
    };

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.doSearch = (termStr = '') => {
        const matchingNodes = doSearch(termStr, vm.searchNodes);
        vm.hierarchy = prepareTree(matchingNodes);
        vm.expandedNodes = prepareExpandedNodes(vm.hierarchy);
    };
}


controller.$inject = [];


export const component = {
    controller,
    bindings,
    template
};


export const id = 'waltzTreePicker';
