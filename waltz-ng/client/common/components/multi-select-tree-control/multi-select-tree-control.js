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
import {initialiseData, invokeFunction} from "../../../common";
import {preventDefault, stopPropagation} from "../../browser-utils"
import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../common/hierarchy-utils";
import template from "./multi-select-tree-control.html";


const bindings = {
    items: "<",
    onClick: "<",
    onCheck: "<",
    onUncheck: "<",
    checkedItemIds: "<",
    expandedItemIds: "<"
};


const initialState = {
    items: [],
    expandedItemIds: [],
    expandedNodes: [],
    checkedItemIds: [],
    checkedMap: {},
    hierarchy: [],
    onCheck: id => console.log("default handler in multi-select-treecontrol for node id check: ", id),
    onUncheck: id => console.log("default handler in multi-select-treecontrol for node id uncheck: ", id),
    onClick: node => console.log("default handler in multi-select-treecontrol for node click: ", node)
};


function expandSearchNotes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function mkCheckedMap(nodes = [], checked = []) {
    return _.reduce(nodes, (acc, n) => {
        acc[n.id] = _.includes(checked, n.id);
        return acc;
    }, {});
}


function expandSelectedNodes(nodes = [], expandedIds = []) {
    function recurse(nodes, ids) {
        const filteredNodes = _.filter(nodes, n => ids.includes(n.id));
        const newParentIds = _.chain(filteredNodes)
            .filter(n => n.parentId)
            .map("parentId")
            .value();

        if(newParentIds.length > 0) {
            return [filteredNodes, recurse(nodes, newParentIds)];
        } else {
            return [filteredNodes];
        }
    }

    return _.flattenDeep(recurse(nodes, expandedIds));
}


function controller() {
    const vm = initialiseData(this, initialState);
    let expandedItemsLatch = false;

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id,
        multiSelection: false,
        isSelectable: (node) => {
            return _.get(node, "concrete", true);
        }
    };

    vm.onNodeClick = (node) => {
        if (node.children && node.children.length > 0) {
            const idx = _.findIndex(vm.expandedNodes, n => n.id === node.id);
            if (idx === -1) {
                // add
                vm.expandedNodes.push(node);
            } else {
                // remove
                vm.expandedNodes.splice(idx, 1);
            }
        }
        invokeFunction(vm.onClick, node.id);
    };

    vm.onNodeCheck = (id) => {
        invokeFunction(vm.onCheck, id);
        preventDefault(event);
        stopPropagation(event);
    };

    vm.onNodeUncheck = (id) => {
        invokeFunction(vm.onUncheck, id);
        preventDefault(event);
        stopPropagation(event);
    };

    vm.$onChanges = changes => {
        if(changes.items) {
            vm.hierarchy = buildHierarchies(vm.items);
            vm.searchNodes = prepareSearchNodes(vm.items);
        }

        if(!expandedItemsLatch && (changes.items || changes.expandedItemIds)) {
            if (vm.expandedItemIds && vm.items) {
                vm.expandedNodes = expandSelectedNodes(vm.items, vm.expandedItemIds);
                expandedItemsLatch = true;
            }
        }

        vm.checkedMap = mkCheckedMap(vm.items, vm.checkedItemIds);
    };

    vm.searchTermsChanged = (termStr = "") => {
        const matchingNodes = doSearch(termStr, vm.searchNodes);
        vm.hierarchy = buildHierarchies(matchingNodes);

        vm.expandedNodes = termStr.length === 0
            ? expandSelectedNodes(vm.items, vm.expandedItemIds)
            : expandSearchNotes(vm.hierarchy);
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged("");
        vm.searchTerms = "";
        vm.expandedNodes = expandSelectedNodes(vm.items, vm.expandedItemIds);
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller,
    transclude: true
};


export default component;