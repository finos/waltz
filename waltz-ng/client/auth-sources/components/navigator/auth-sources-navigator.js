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

import template from './auth-sources-navigator.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import AuthSourcesNavigatorUtil from '../../services/auth-source-navigator-utils';
import {AUTH_SOURCE_NAVIGATOR_CATEGORY_ID} from "../../../system/services/settings-names";

const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    categoryId: null,
    visibility: {
        chart: false
    }
};


function controller($element, $timeout, $q, serviceBroker, settingsService) {
    const vm = initialiseData(this, initialState);

    const loadChartData = () => {

        const promises = [
            serviceBroker
                .loadViewData(CORE_API.AuthSourcesStore.findAuthSources, [ vm.parentEntityRef ])
                .then(r => r.data),
            serviceBroker
                .loadAppData(CORE_API.DataTypeStore.findAll)
                .then(r => r.data),
            serviceBroker
                .loadAppData(CORE_API.MeasurableStore.findAll)
                .then(r => r.data),
            serviceBroker
                .loadViewData(CORE_API.MeasurableRatingStore.findByCategory, [ vm.category.id ])
                .then(r => r.data)
        ];

        $q.all(promises)
            .then(([authSources, dataTypes, allMeasurables, allRatings]) => {
                const measurables = _.filter(allMeasurables, { categoryId: vm.category.id });
                const navigator = new AuthSourcesNavigatorUtil(dataTypes, measurables, authSources, allRatings);
                const chart = navigator.refresh();
                vm.visibility.chart = chart.rowGroups.length > 0;
                vm.chartNavigator = navigator;
            });
    };

    vm.$onInit = () => {
        const promises = [
            serviceBroker
                .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
                .then(r => r.data),
            settingsService
                .findOrDie(AUTH_SOURCE_NAVIGATOR_CATEGORY_ID, 'Cannot find auth source navigator measurable category')
        ];

        $q.all(promises)
            .then(([categories, defaultCategoryId]) => {
                vm.categories = categories;
                vm.categoriesById = _.keyBy(categories, 'id');
                vm.category = vm.categoriesById[defaultCategoryId];
                loadChartData();
            });
    };

    vm.onCategoryChange = () => {
        loadChartData();
    };
}


controller.$inject = [
    '$element',
    '$timeout',
    '$q',
    'ServiceBroker',
    'SettingsService'
];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzAuthSourcesNavigator',
    component
}