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
import {buildHierarchies, switchToParentIds} from '../../../common/hierarchy-utils';

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


const template = require('./measurable-ratings-browser.html');


function mkEmptyRatingTally() {
    return { R: 0, A: 0, G: 0, Z: 0, total: 0};
}


function addRatingTallies(r1 = mkEmptyRatingTally(),
                    r2 = mkEmptyRatingTally()) {

    return {
        R: (r1.R || 0) + (r2.R || 0),
        A: (r1.A || 0) + (r2.A || 0),
        G: (r1.G || 0) + (r2.G || 0),
        Z: (r1.Z || 0) + (r2.Z || 0),
        total: (r1.total || 0) + (r2.total || 0)
    };
}


function toRatingsSummaryObj(ratings = []) {
    const byRating = _.keyBy(ratings, 'rating');
    const r = _.get(byRating, 'R.count', 0);
    const a = _.get(byRating, 'A.count', 0);
    const g = _.get(byRating, 'G.count', 0);
    const z = _.get(byRating, 'Z.count', 0);
    const total = r + a + g + z;

    return {
        R: r,
        A: a,
        G: g,
        Z: z,
        total
    };
}


function prepareTreeData(data = []) {
    return switchToParentIds(buildHierarchies(data));
}


function prepareTabs(categories = [], measurables = []) {
    const byCategory = _.groupBy(measurables, 'categoryId');

    const tabs = _.map(categories, category => {
        const treeData = prepareTreeData(byCategory[category.id]);
        const maxSize = _.chain(treeData)
            .map('totalRatings.total')
            .max()
            .value();

        return {
            category,
            treeData,
            maxSize,
            expandedNodes: []
        };
    });

    return _.sortBy(tabs, 'category.name');
}


function findFirstNonEmptyTab(tabs = []) {
    const firstNonEmptyTab = _.find(tabs, t => t.treeData.length > 0);
    return _.get(firstNonEmptyTab || tabs[0], 'category.id');
}


function initialiseRatingTalliesMap(ratingTallies, measurables) {
    const talliesById = _.groupBy(ratingTallies, 'id');

    const reducer = (acc, m) => {
        const summaryObj = talliesById[m.id]
            ? toRatingsSummaryObj(talliesById[m.id])
            : mkEmptyRatingTally();

        acc[m.id] = {
            direct: _.clone(summaryObj),
            total: _.clone(summaryObj),
        };
        return acc;
    };
    const talliesMap = _.reduce(measurables, reducer, {});
    return talliesMap;
}


function mkRatingTalliesMap(ratingTallies = [], measurables = []) {
    const measurablesById = _.keyBy(measurables, 'id');
    const talliesMap = initialiseRatingTalliesMap(ratingTallies, measurables);

    _.each(measurables, m => {
        const rs = talliesMap[m.id];
        while (m.parentId) {
            const parent = measurablesById[m.parentId];
            const parentRating = talliesMap[m.parentId];
            parentRating.total = addRatingTallies(parentRating.total, rs.direct);
            m = parent;
        }
    });

    return talliesMap;
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (vm.measurables && vm.ratingTallies && vm.categories) {
            vm.tabs = prepareTabs(vm.categories, vm.measurables);
            vm.ratingsMap = mkRatingTalliesMap(vm.ratingTallies, vm.measurables);
            vm.visibility.tab = findFirstNonEmptyTab(vm.tabs);
            vm.maxTotal = _.max(
                _.map(
                    _.values(vm.ratingsMap),
                    r => _.get(r, 'total.total'), 0));
        }

        if (c.scrollHeight) {
            vm.containerClass = [
                `waltz-scroll-region-${vm.scrollHeight}`
            ];
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