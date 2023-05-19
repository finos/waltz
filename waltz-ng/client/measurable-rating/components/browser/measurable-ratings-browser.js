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
import {buildHierarchies} from "../../../common/hierarchy-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {distinctRatingCodes, indexRatingSchemes} from "../../../ratings/rating-utils";
import template from "./measurable-ratings-browser.html";


/**
 * @name waltz-measurable-ratings-browser
 *
 * @description
 * This component ...
 */


const bindings = {
    measurables: '<',
    categories: '<',
    ratingTallies: '<',
    onSelect: '<',
    onSelectUnmapped: '<?',
    onCategorySelect: '<',
    scrollHeight: '<'
};


const initialState = {
    containerClass: [],
    measurables: [],
    ratingTallies: [],
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
    onSelect: (d) => console.log('wmrb: default on-select', d),
    onSelectUnmapped: null, // This will cause the 'view unmapped measurables' label not to render
    visibility: {
        tab: null
    }
};


function combineRatingTallies(r1, r2) {
    return _.mergeWith(
        {}, r1, r2,
        (v1, v2) => (v1 || 0) + (v2 || 0));
}


function toRatingsSummaryObj(ratings = []) {

    const counts = _.chain(ratings)
        .groupBy(r => r.rating)
        .mapValues(r => {

            const countsForRating = _.map(_.values(r), r => r.count);
            return _.sum(countsForRating);
        })
        .value();

    const total = _.sum(_.values(counts));
    return Object.assign({}, counts, { total });
}


function prepareTreeData(data = []) {
    return buildHierarchies(data, false);
}


function prepareTabs(categories = [], measurables = [], ratingSchemesById = {}) {
    const measurablesByCategory = _.groupBy(measurables, 'categoryId');
    return _
        .chain(categories)
        .filter(category => _.get(measurablesByCategory, category.id, []).length > 0)
        .map(category => {
            const measurablesForCategory = measurablesByCategory[category.id];
            const treeData = prepareTreeData(measurablesForCategory);
            const maxSize = _.chain(treeData)
                .map('totalRatings.total')
                .max()
                .value();

            return {
                category,
                ratingScheme: ratingSchemesById[category.ratingSchemeId],
                treeData,
                maxSize,
                expandedNodes: []
            };
        })
        .sortBy(d => d.category.position, d => d.category.name)
        .value();
}


function findFirstNonEmptyTab(tabs = []) {
    const firstNonEmptyTab = _.find(tabs, t => t.treeData.length > 0);
    return firstNonEmptyTab || tabs[0];
}


function initialiseRatingTalliesMap(ratingTallies = [], measurables = []) {
    const talliesById = _.groupBy(ratingTallies, 'id');

    const reducer = (acc, m) => {
        const talliesForMeasurable = talliesById[m.id];
        const summaryObj = talliesForMeasurable
            ? toRatingsSummaryObj(talliesForMeasurable)
            : {};

        acc[m.id] = {
            direct: _.clone(summaryObj),
            compound: _.clone(summaryObj),
        };
        return acc;
    };
    return _.reduce(measurables, reducer, {});
}


function mkRatingTalliesMap(ratingTallies = [], measurables = []) {
    const measurablesById = _.keyBy(measurables, 'id');
    const talliesMap = initialiseRatingTalliesMap(ratingTallies, measurables);
    _.each(measurables, m => {
        const rs = talliesMap[m.id];

        while (m.parentId) {
            const parent = measurablesById[m.parentId];
            if (! parent) break;
            const parentRating = talliesMap[m.parentId];
            parentRating.compound = combineRatingTallies(parentRating.compound, rs.direct);
            m = parent;
        }
    });

    return talliesMap;
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const prepareData = () => {
        if (_.isEmpty(vm.measurables) ||
            _.isEmpty(vm.ratingTallies) ||
            _.isEmpty(vm.categories) ||
            _.isEmpty(vm.ratingSchemesById)) {

            if (_.isEmpty(vm.ratingTallies)) {
                vm.ratingsMap = {};
            }
        } else {
            const tabs = prepareTabs(vm.categories, vm.measurables, vm.ratingSchemesById);
            const tab = findFirstNonEmptyTab(tabs);

            vm.tabs = tabs;

            vm.ratingsMap = mkRatingTalliesMap(vm.ratingTallies, vm.measurables);
            vm.maxTotal = _.max(
                _.flatMap(
                    _.values(vm.ratingsMap),
                    r => _.get(r, ["compound", "total"], [0])));

            if (! vm.visibility.tab) {
                // no tab selected, select the first
                vm.visibility.tab = _.get(tab, ["category", "id"]);
                vm.onTabChange(tab);
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
        invokeFunction(vm.onCategorySelect, tc.category);
    };
}


controller.$inject = [
    'ServiceBroker'
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
