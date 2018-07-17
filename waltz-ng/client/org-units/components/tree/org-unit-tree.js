
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

