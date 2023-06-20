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
import {initialiseData, invokeFunction} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {distinctRatingCodes, indexRatingSchemes} from "../../../ratings/rating-utils";
import template from "./measurable-ratings-browser.html";
import {findFirstNonEmptyTab, findMaxTotal, mkRatingTalliesMap, prepareTabs} from "./measurable-ratings-browser-utils";


/**
 * @name waltz-measurable-ratings-browser
 *
 * @description
 * This component ...
 */


const bindings = {
    measurables: "<",
    categories: "<",
    ratingTallies: "<",
    lastViewedCategoryId: "<?",
    onSelect: "<",
    onSelectUnmapped: "<?",
    onCategorySelect: "<",
    scrollHeight: "<"
};


const initialState = {
    containerClass: [],
    measurables: [],
    ratingTallies: [],
    treeOptions: {
        nodeChildren: "children",
        dirSelectable: true,
        equality: function (node1, node2) {
            if (node1 && node2) {
                return node1.id === node2.id;
            } else {
                return false;
            }
        }
    },
    onSelect: (d) => console.log("wmrb: default on-select", d),
    onSelectUnmapped: null, // This will cause the 'view unmapped measurables' label not to render
    visibility: {
        tab: null
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const prepareData = () => {
        if (_.isEmpty(vm.measurables) ||
            _.isEmpty(vm.categories) ||
            _.isEmpty(vm.ratingSchemesById)) {

            if (_.isEmpty(vm.ratingTallies)) {
                vm.ratingsMap = {};
            }
        } else {
            vm.ratingsMap = mkRatingTalliesMap(vm.ratingTallies, vm.measurables);
            vm.maxTotal = findMaxTotal(vm.ratingsMap);

            const unusedMeasurables = _
                .chain(vm.ratingsMap)
                .values()
                .filter(d => _.get(d, ["compound", "total"], 0) === 0)
                .map(d => d.measurableId)
                .value();

            const tabs = prepareTabs(
                vm.categories,
                _.reject(vm.measurables, m => _.includes(unusedMeasurables, m.id)),
                vm.ratingSchemesById);

            const lastViewedCategory = _.find(tabs, t => t.category.id === vm.lastViewedCategoryId);
            const startingTab = lastViewedCategory || findFirstNonEmptyTab(tabs);

            vm.tabs = tabs;

            if (!vm.visibility.tab) {
                // no startingTab selected, select the last viewed or first
                vm.visibility.tab = _.get(startingTab, ["category", "id"]);
                vm.onTabChange(startingTab);
            }
        }
    };

    vm.$onInit = () => {
        return serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => {
                vm.ratingSchemesById = indexRatingSchemes(r.data);
                vm.distinctRatingCodes = distinctRatingCodes(r.data);
            })
            .then(() => prepareData());
    };

    vm.$onChanges = (c) => {
        if (c.scrollHeight) {
            vm.containerClass = [
                `waltz-scroll-region-${vm.scrollHeight}`
            ];
        }
        prepareData();
    };

    vm.onTabChange = (tc) => {
        if (_.isNil(tc)) {
            // no tab available yet, do nothing
        } else {
            invokeFunction(vm.onCategorySelect, tc.category);
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableRatingsBrowser"
};
