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
import {buildHierarchies, initialiseData, switchToParentIds} from '../../../common';
import {buildPropertySummer} from '../../../common/tally-utils';
import {scaleLinear} from 'd3-scale';


/**
 * @name waltz-measurable-tree
 *
 * @description
 * This component ...
 */


const bindings = {
    measurables: '<',
};


const initialState = {
    expandedNodes: [],
    hierarchy: [],
    searchNodes: [],
    searchTerms: '',
    chartScale: () => 0,
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
    }
};


const template = require('./measurable-tree.html');


const recursivelySum = buildPropertySummer();


function prepareTree(measurables = []) {
    const hierarchy = switchToParentIds(buildHierarchies(measurables));
    _.each(hierarchy, root => recursivelySum(root));

    return hierarchy;
}


function prepareExpandedNodes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function prepareChartScale(hierarchy) {
    const maxCount = _.get(
            _.maxBy(hierarchy, 'totalCount'),
            'totalCount') || 0;
    return scaleLinear()
        .range([0, 100])
        .domain([0, maxCount])
}


function prepareSearchNodes(nodes = []) {
    const nodesById = _.keyBy(nodes, 'id');

    return _.map(nodes, n => {
        let ptr = n;
        let searchStr = '';
        const nodePath = [];
        while (ptr) {
            nodePath.push(ptr);
            searchStr += ptr.name + ' ';
            ptr = nodesById[ptr.parentId];
        }
        return {
            searchStr: searchStr.toLowerCase(),
            nodePath
        };
    });
}


function doSearch(termStr = '', searchNodes = []) {
    const terms = _.split(termStr.toLowerCase(), /\W+/);

    return _
        .chain(searchNodes)
        .filter(sn => {
            const noTerms = termStr.trim().length === 0;
            const allMatch = _.every(terms, t => sn.searchStr.indexOf(t) >=0)
            return noTerms || allMatch;
        })
        .flatMap('nodePath')
        .uniqBy(n => n.id)
        .value();
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.searchTermsChanged = (termStr = '') => {
        vm.hierarchy = prepareTree(doSearch(termStr, vm.searchNodes));
    };

    vm.$onChanges = (c) => {
        if (c.measurables) {
            vm.searchNodes = prepareSearchNodes(vm.measurables);
            vm.hierarchy = prepareTree(vm.measurables);
            vm.expandedNodes = prepareExpandedNodes(vm.hierarchy);
            vm.chartScale = prepareChartScale(vm.hierarchy);
        }
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;
