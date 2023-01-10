/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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
    expandedNodes: "<?",
    hideSearch: "<?",
    linkToState: "@?"
};


const initialState = {
    linkToState: "main.measurable.view",
    expandedNodes: [],
    hierarchy: [],
    searchNodes: [],
    searchTerms: "",
    chartScale: () => 0,
    hideSearch: false,
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
        if (termStr === "") {
            vm.hierarchy = prepareTree(vm.measurables);
            vm.expandedNodes = [];
        } else {
            const matchedNodes = doSearch(termStr, vm.searchNodes);
            vm.hierarchy = prepareTree(matchedNodes);

            vm.expandedNodes = determineExpandedNodes(
                vm.hierarchy,
                determineDepthLimit(matchedNodes.length));
        }
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged("");
        vm.searchTerms = "";
    };

    vm.$onChanges = (c) => {
        if (c.measurables) {
            vm.searchNodes = prepareSearchNodes(vm.measurables);
            vm.hierarchy = prepareTree(vm.measurables);
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
