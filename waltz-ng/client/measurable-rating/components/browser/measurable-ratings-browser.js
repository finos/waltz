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
    const counts = _.countBy(ratings, 'rating');
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
        .sortBy('category.name')
        .value();
}


function findFirstNonEmptyTab(tabs = []) {
    const firstNonEmptyTab = _.find(tabs, t => t.treeData.length > 0);
    return _.get(firstNonEmptyTab || tabs[0], 'category.id');
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
        } else {
            vm.tabs = prepareTabs(vm.categories, vm.measurables, vm.ratingSchemesById);
            vm.ratingsMap = mkRatingTalliesMap(vm.ratingTallies, vm.measurables);
            vm.visibility.tab = findFirstNonEmptyTab(vm.tabs);
            vm.maxTotal = _.max(
                _.flatMap(
                    _.values(vm.ratingsMap),
                    r => _.get(r, 'compound.total', [0])));
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
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default component;