/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from 'lodash';
import {CORE_API} from "../../common/services/core-api-utils";
import {prepareSearchNodes, doSearch, buildHierarchies, switchToParentIds} from '../../common/hierarchy-utils';


const bindings = {
    onSelection: '<'
};


const template = require('./data-type-tree.html');


function prepareTree(dataTypes = []) {
    const hierarchy = switchToParentIds(buildHierarchies(dataTypes));
    return hierarchy;
}


function prepareExpandedNodes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function controller(serviceBroker) {
    const vm = this;

    serviceBroker
        .loadAppData(CORE_API.DataTypeStore.findAll, [])
        .then(r => {
            const dataTypes = r.data;
            vm.hierarchy = prepareTree(dataTypes);
            vm.searchNodes = prepareSearchNodes(dataTypes);
        });


    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.searchTermsChanged = (termStr = '') => {
        const matchingNodes = doSearch(termStr, vm.searchNodes);
        vm.hierarchy = prepareTree(matchingNodes);
        vm.expandedNodes = prepareExpandedNodes(vm.hierarchy);
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged('');
        vm.searchTerms = '';
    };

}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


export default component;