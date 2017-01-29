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
    sourceDataRatings: '<'
};


const template = require('./measurable-rating-app-section.html');


const initialState = {
    ratings: [],
    categories: [],
    measurables: [],
    visibility: {
        overlay: false,
        tab: null
    },
    byCategory: {}
};




function groupByCategory(categories = [], measurables = [], ratings = []) {

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
            ratings: ratingsForMeasure
        };
    });

    return _.sortBy(
        grouped,
        g => g.category.name);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.measurables || c.ratings || vm.categories) {
            vm.tabs = groupByCategory(vm.categories, vm.measurables, vm.ratings);
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