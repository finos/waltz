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
import {initialiseData} from "../../../common";
import {buildPropertySummer} from "../../../common/tally-utils";
import {scaleLinear} from "d3-scale";
import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../common/hierarchy-utils";
import template from "./measurable-tree.html";


/**
 * @name waltz-measurable-tree
 *
 * @description
 * This component displays a measurable hierarchy in a tree control.
 * By default each node in the tree will link to the corresponding
 * measurable view page.  This behaviour can be overriden by
 * passing in either an `on-select` callback function or by
 * passing in a `link-to-state` name
 */


const bindings = {
    measurables: "<",
    onSelect: "<?",
    linkToState: "@?"
};


const initialState = {
    linkToState: "main.measurable.view",
    expandedNodes: [],
    hierarchy: [],
    searchNodes: [],
    searchTerms: "",
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


const recursivelySum = buildPropertySummer();


function prepareTree(measurables = []) {
    const hierarchy = buildHierarchies(measurables, false);
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
            _.maxBy(hierarchy, "totalCount"),
            "totalCount") || 0;
    return scaleLinear()
        .range([0, 100])
        .domain([0, maxCount])
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.searchTermsChanged = (termStr = "") => {
        vm.hierarchy = prepareTree(doSearch(termStr, vm.searchNodes));
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged("");
        vm.searchTerms = "";
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


export default {
    component,
    id: "waltzMeasurableTree"
};
