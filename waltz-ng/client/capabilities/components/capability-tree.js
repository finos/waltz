
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import {buildHierarchies, invokeFunction} from "../../common";
import _ from 'lodash';


const bindings = {
    capabilities: '<',
    options: '<',
    filterExpression: '@',
    selectedNode: '<',
    nodeSelect: '<',
    disabledNodeIds: '<'
};


// tree widget does deep comparisons.
// Having parents as refs blows the callstack
// therefore replace refs with id's.
function switchToParentIds(treeData = []) {
    _.each(treeData, td => {
        td.parent = td.parent ? td.parent.id : null;
        switchToParentIds(td.children);
    });
    return treeData;
}


function prepareTreeData(capabilities = [], disabledNodeIds = []) {
    const enrichedCapabilities = _.map(
        capabilities,
        c => Object.assign(
            {},
            c ,
            { disabled: _.includes(disabledNodeIds, c.id ) }));

    const treeData = buildHierarchies(enrichedCapabilities);
    return switchToParentIds(treeData);
}


function expandSelection(capabilities = [], selected) {
    if (! selected) return [];

    const byId = _.keyBy(capabilities, 'id');

    const nodesToExpand = [
        byId[selected.level1],
        byId[selected.level2],
        byId[selected.level3],
        byId[selected.level4],
        byId[selected.level5],
    ].filter(n => n != null);

    return nodesToExpand;
}


function controller() {

    const vm = this;

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: function(node1, node2) {
            if (node1 && node2) {
                return node1.id === node2.id;
            } else {
                return false;
            }
        }
    };

    vm.treeData = [];
    vm.expandedNodes = [];

    vm.$onChanges = (changes) => {
        vm.treeData = prepareTreeData(vm.capabilities, vm.disabledNodeIds);
        if (changes.selectedNode) {
            vm.expandedNodes = _.union(vm.expandedNodes, expandSelection(vm.capabilities, vm.selectedNode));
        }
        if (vm.disabledNodeIds) {
            vm.treeOptions.isSelectable = (n) => ! _.includes(vm.disabledNodeIds, n.id);
        }
    };

    vm.toggleExpansion = (node) => {
        // tree expansion state is in shared structure 'expandedNodes'
        const idx = _.indexOf(_.map(vm.expandedNodes, 'id'), node.id);
        if (idx === -1) {
            // not found, therefore expand
            vm.expandedNodes.push(node);
        } else {
            // found, therefore collapse
            vm.expandedNodes.splice(idx, 1);
        }
    };

    vm.collapseAll = () => {
        vm.expandedNodes.length = 0;
    };

    vm.onNodeSelect = (n) => invokeFunction(vm.nodeSelect, n);
}

controller.$inject = [];


const component = {
    template: require('./capability-tree.html'),
    bindings,
    controller
};


export default component;
