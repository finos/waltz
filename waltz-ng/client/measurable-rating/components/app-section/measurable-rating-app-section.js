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
import {mergeKeyedLists, toGroupedMap} from '../../../common/map-utils';

/**
 * @name waltz-measurable-rating-panel
 *
 * @description
 * This component render multiple <code>measurable-rating-panel</code> components
 * within a tab group.
 *
 * It is intended to be used to show measurables and ratings for a single application.
 */


const bindings = {
    application: '<',
    categories: '<',
    measurables: '<',
    ratings: '<',
    perspectiveDefinitions: '<',
    perspectiveRatings: '<',
    sourceDataRatings: '<'
};


const template = require('./measurable-rating-app-section.html');


const initialState = {
    ratings: [],
    perspectiveDefinitions: [],
    perspectiveRatings: [],
    categories: [],
    measurables: [],
    visibility: {
        overlay: false,
        tab: null
    },
    byCategory: {}
};




function mkTabs(categories = [], measurables = [], ratings = [], perspectiveDefinitions = []) {

    const measurablesByCategory = _.groupBy(
        measurables,
        'categoryId');

    const grouped = _.map(categories, category => {
        const usedMeasurables = measurablesByCategory[category.id] || [];

        const measurableIds = _.map(usedMeasurables, 'id');
        const ratingsForMeasure = _.filter(
            ratings,
            r => _.includes(measurableIds, r.measurableId));

        return {
            category,
            measurables: usedMeasurables,
            ratings: ratingsForMeasure,
            perspectiveDefinitions
        };
    });

    return _.sortBy(
        grouped,
        g => g.category.name);
}


function mkOverridesMap(perspectiveRatings = [], measurables = []) {
    const ratings = _.map(perspectiveRatings, 'value');
    const measurablesById = _.keyBy(measurables, 'id');

    const byX = toGroupedMap(
        ratings,
        r => r.measurableX,
        r => ({
            measurable: measurablesById[r.measurableY],
            rating: r.rating
        }));
    const byY = toGroupedMap(
        ratings,
        r => r.measurableY,
        r => ({
            measurable: measurablesById[r.measurableX],
            rating: r.rating
        }));

    return mergeKeyedLists(byX, byY);
}


function controller() {
    const vm = this;

    vm.$onInit = () => {
        initialiseData(vm, initialState);
    };

    vm.$onChanges = () => {
        if (vm.perspectiveRatings && vm.measurables) {
            vm.overridesByMeasurableId = mkOverridesMap(vm.perspectiveRatings, vm.measurables);
        }

        if (vm.measurables && vm.ratings && vm.categories) {
            vm.tabs = mkTabs(vm.categories, vm.measurables, vm.ratings, vm.perspectiveDefinitions);
            const firstNonEmptyTab = _.find(vm.tabs, t => t.ratings.length > 0);
            vm.visibility.tab = firstNonEmptyTab ? firstNonEmptyTab.category.id : null;
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