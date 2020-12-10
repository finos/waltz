
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
import {isEmpty} from "../../../common";
import template from './org-unit-tree.html';


const bindings = {
    tree: '<'
};


/**
 * Pre-expand the tree a little bit
 * @param tree
 * @returns {Array}
 */
function expandTree(tree = []) {
    if (isEmpty(tree)) return;
    const expandedNodes =  _.concat(tree, tree[0].children);
    return expandedNodes;
}


const treeOptions =  {
    nodeChildren: "children",
    equality: (a, b) => a && b && a.id === b.id
};


function controller() {
    const vm = this;

    vm.expandedNodes = [];

    vm.treeOptions = treeOptions;

    vm.hasOwnApps = (node) => node.appCount && node.appCount > 0;
    vm.hasAnyApps = (node) => node.totalAppCount && node.totalAppCount > 0;
    vm.hasInheritedApps = (node) => node.childAppCount && node.childAppCount > 0;

    vm.hasOwnEndUserApps = (node) => node.endUserAppCount && node.endUserAppCount > 0;
    vm.hasAnyEndUserApps = (node) => node.totalEndUserAppCount && node.totalEndUserAppCount > 0;
    vm.hasInheritedEndUserApps = (node) => node.childEndUserAppCount && node.childEndUserAppCount > 0;

    vm.$onChanges = (changes) => {
        if (changes.tree) {
            _.each(
                expandTree(vm.tree),
                n => vm.expandedNodes.push(n));
        }
    };

}

controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzOrgUnitTree'
};

