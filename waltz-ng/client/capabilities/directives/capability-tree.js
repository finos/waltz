
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

import { buildHierarchies } from '../../common';

const BINDINGS = {
    capabilities: '=',
    options: '=',
    filterExpression: '@',
    selectedNode: '='
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


function controller($scope, $filter) {

    const vm = this;

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true
    };

    vm.treeData = [];
    vm.expandedNodes = [];

    $scope.$watch('ctrl.capabilities', (capabilities = []) => {
        const treeData = buildHierarchies(capabilities);
        vm.treeData = switchToParentIds(treeData);
    });

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

    vm.expandFiltered = () => {

        const flatten = (nodes = [], acc = {}) => {
            return _.foldr(nodes, (acc, n) => {
                acc[n.id] = n;
                if (n.children) { flatten(n.children, acc); }
                return acc;
            }, acc);
        };

        const nodesById = flatten(vm.treeData);

        const nodeIdsToExpand = [];

        console.log(nodesById, _.values(nodesById).length);

        const results = $filter('filter')(_.values(nodesById), vm.filterExpression);

        vm.expandedNodes.push(...results);

        console.log(_.map(results, 'name'));
    }

}

controller.$inject = ['$scope', '$filter'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./capability-tree.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
