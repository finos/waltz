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

import _ from 'lodash';
import {initialiseData, buildHierarchies, switchToParentIds} from '../../../common';


/**
 * @name waltz-measurable-rating-tree
 *
 * @description
 * Tree control used to show measurables and their ratings.
 *
 * Intended only for use with a single application and a single  measurable kind.
 */


const bindings = {
    ratings: '<',
    measurables: '<',
    onSelect: '<'
};


const initialState = {
    measurables: [],
    ratings: [],
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
    onSelect: d => console.log('default on-select for measurable-rating-tree: ', d)
};


const template = require('./measurable-rating-tree.html');


function enrichNodes(ratings = [], measurables = []) {
    const ratingsByMeasurableId = _.keyBy(ratings, 'measurableId');
    return _.map(measurables, m =>
        Object.assign(
            {}, m, { rating: ratingsByMeasurableId[m.id] }));
}


// expand nodes with a rating (incl. parents)
function calculateExpandedNodes(nodes = []) {
    const byId = _.keyBy(nodes, 'id');
    const startingNodes = _.filter(nodes, n => n.rating);

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


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const nodes = enrichNodes(vm.ratings, vm.measurables);
        vm.treeData = switchToParentIds(buildHierarchies(nodes));
        vm.expandedNodes = calculateExpandedNodes(nodes);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};

export default component;