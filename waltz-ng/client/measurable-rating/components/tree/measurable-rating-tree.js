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
import {initialiseData} from '../../../common';
import {buildHierarchies, switchToParentIds, prepareSearchNodes, doSearch} from '../../../common/hierarchy-utils';



/**
 * @name waltz-measurable-rating-tree
 *
 * @description
 * Tree control used to show measurables and their ratings.
 *
 * Intended only for use with a single application and a single measurable kind.
 */
const bindings = {
    ratings: '<',
    measurables: '<',
    onSelect: '<',
    scrollHeight: '@' // should correspond to numeric values in `waltz-scroll-region` classes
};


const initialState = {
    containerClass: '',
    hierarchy: [],
    measurables: [],
    ratings: [],
    searchTerms: '',
    treeOptions: {
        nodeChildren: "children",
        dirSelectable: true,
        equality: function(node1, node2) {
            if (node1 && node2) {
                return node1.id === node2.id;
            } else {
                return false;
            }
        }
    },
    onSelect: (m, r) => console.log('default on-select for measurable-rating-tree: ', m, r)
};


const template = require('./measurable-rating-tree.html');


// expand nodes with a rating (incl. parents)
function calculateExpandedNodes(nodes = [], ratingsById = {}) {
    const byId = _.keyBy(nodes, 'id');
    const startingNodes = _.filter(nodes, n => ratingsById[n.id] != null);

    const requiredIds = [];
    _.each(startingNodes, n => {
        requiredIds.push(n.id);
        while (n.parentId) {
            requiredIds.push(n.parentId);
            n = byId[n.parentId];
        }
    });

    // de-dupe and resolve
    return _.map(
        _.uniq(requiredIds),
        id => byId[id]);
}


function prepareTree(nodes = []) {
    return switchToParentIds(buildHierarchies(nodes));

}

function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        vm.searchNodes = prepareSearchNodes(vm.measurables);
        vm.hierarchy = prepareTree(vm.measurables);
        vm.ratingsByMeasurable = _.keyBy(vm.ratings || [], 'measurableId');

        if (_.isEmpty(vm.expandedNodes)) {
            vm.expandedNodes = calculateExpandedNodes(vm.measurables, vm.ratingsByMeasurable);
        }

        if (c.scrollHeight) {
            vm.containerClass = `waltz-scroll-region-${vm.scrollHeight}`;
        }
    };

    vm.searchTermsChanged = (termStr = '') => {
        vm.hierarchy = prepareTree(doSearch(termStr, vm.searchNodes));
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged('');
        vm.searchTerms = '';
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;