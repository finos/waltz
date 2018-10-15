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

import _ from 'lodash';
import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from "../../../common/index";
import template from './app-costs-section.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    currentYear: null,
    costsMostRecentYear: null,
    mostRecentCosts : [],
    mostRecentTotal : null
};


function getCurrentYear(costs = []) {
    const defaultYear = new Date().getFullYear();
    const foundYear = _.chain(costs)
            .map('cost.year')
            .max()
            .value();
    return foundYear || defaultYear;
}


function calcTotalCost(costs = []) {
    return _.sumBy(costs, 'cost.amount').toFixed(2);
}


function filterCostsForYear(year, costs = []) {
    return _.filter(costs, c => c.cost.year === year);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        serviceBroker
            .loadViewData(
                CORE_API.AssetCostStore.findByAppId,
                [vm.parentEntityRef.id])
            .then(r => {
                vm.currentYear = (new Date()).getFullYear();
                vm.costs = r.data;
                vm.costsMostRecentYear = getCurrentYear(vm.costs);
                vm.mostRecentCosts = filterCostsForYear(vm.costsMostRecentYear, vm.costs);
                vm.mostRecentTotal = calcTotalCost(vm.mostRecentCosts);
            });
    };

    vm.$onInit = () => {

        serviceBroker
            .loadAppData(CORE_API.SourceDataRatingStore.findAll)
            .then(r => vm.sourceDataRatings = r.data);

        refresh();
        vm.$onChanges = refresh;
    };

}


controller.$inject = [
    'ServiceBroker',
];


const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: 'waltzAppCostsSection'
};
