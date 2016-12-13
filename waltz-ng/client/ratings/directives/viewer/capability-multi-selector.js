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

import {switchToParentIds, populateParents} from "../../../common";


const BINDINGS = {
    capabilities: '=',
    explicitCapabilityIds: '=',
    initiallySelectedIds: '=',
    selectedNodes: '=',
    onSelect: '='
};


function hasChildren(node) {
    return ! _.isEmpty(node.children);
}


function calcChildrenIds(node) {
    if (! hasChildren(node)) return [];

    const childIds = [];

    const add = (nodes) => {
        _.each(nodes, n => {
            childIds.push(n.id);
            if (hasChildren(n)) {
                add(n.children);
            }
        });
    };

    add([node]);
    return childIds;
}


function deselectAllFn(selectedNodes) {
    return (node, $event) => {
        $event.stopPropagation();
        const childIds = calcChildrenIds(node);
        _.each(childIds, childId => {
            const selectedIds = _.map(selectedNodes, 'id');
            const idx = _.indexOf(selectedIds, childId);
            if (idx > -1) {
                selectedNodes.splice(idx, 1);
            }
        });
    };
}


function selectAllFn(selectedNodes, nodesById, allowableNodeIds) {
    return (node, $event) => {
        $event.stopPropagation();

        const idsToAdd = _.chain(calcChildrenIds(node))
            .filter(id => _.includes(allowableNodeIds, id))
            .value();

        _.each(idsToAdd, id => {
            const currentlySelectedIds = _.map(selectedNodes, 'id');
            const idx = _.indexOf(currentlySelectedIds, id);
            if (idx === -1) {
                selectedNodes.push(nodesById[id]);
            }
        });
    };
}


function setupTreeOptions() {
    return {
        nodeChildren: "children",
        multiSelection: true,
        dirSelectable: true,
        equality: (n1, n2) => (n1 && n2 ? n1.id === n2.id : false),
        expanded: [1]
    };
}


const WATCH_EXPRESSIONS = [
    'ctrl.capabilities',
    'ctrl.explicitCapabilityIds',
    'ctrl.initiallySelectedIds'
];


function populateSelectedNodes(ids, selectedNodes, nodesById) {
    _.each(ids, id => selectedNodes.push(nodesById[id]));
}


function controller($scope) {

    const vm = this;
    vm.treeData = [];
    vm.treeOptions = setupTreeOptions();


    $scope.$watchGroup(
        WATCH_EXPRESSIONS,
        ([capabilities = [], explicitCapabilityIds = [], initiallySelectedIds = []]) => {
            const nodeData = switchToParentIds(populateParents(capabilities));
            const nodesById = _.keyBy(nodeData, 'id');

            vm.treeData = _.filter(nodeData, n => ! n.parentId);
            vm.expandedNodes = [];
            vm.explicitCapabilityIds = explicitCapabilityIds;
            vm.nodesById = nodesById;
            vm.selectAll = selectAllFn(vm.selectedNodes, vm.nodesById, vm.explicitCapabilityIds);
            vm.deselectAll = deselectAllFn(vm.selectedNodes);
            vm.treeOptions.isSelectable = (n) => _.includes(vm.explicitCapabilityIds, n.id);

            populateSelectedNodes(initiallySelectedIds, vm.selectedNodes, nodesById);
        });

    vm.hasChildren = hasChildren;
}

controller.$inject = [ '$scope' ];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./capability-multi-selector.html')
});
