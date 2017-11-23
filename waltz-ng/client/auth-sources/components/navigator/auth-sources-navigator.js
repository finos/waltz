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

const bindings = {
    parentEntityRef: '<',
    measurableCategoryId: '<'
};


const initialState = {
};


function controller($element, $timeout, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = async () => {
        console.log('vm', vm)
        const selector = mkSelectionOptions(vm.parentEntityRef);

        const authSources = await serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findAuthSources, [ vm.parentEntityRef ])
            .then(r => r.data);
        const dataTypes = await serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => r.data);
        const allMeasurables = await serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => _.filter(r.data, { categoryId: vm.measurableCategoryId }));
        const allRatings = await serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findByCategory, [ vm.measurableCategoryId ])
            .then(r => r.data);

        const navigator = new AuthSourcesNavigatorUtil(dataTypes, allMeasurables, authSources, allRatings);

        vm.chartNavigator = navigator;

        vm.focusBoth = (xId, yId) => {
            vm.chart = navigator.focusBoth(xId, yId);
        };

        $timeout(() => vm.focusBoth(null, null));
    };

}


controller.$inject = ['$element', '$timeout', 'ServiceBroker'];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzAuthSourcesNavigator',
    component
}