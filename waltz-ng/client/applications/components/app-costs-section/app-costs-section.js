/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from 'lodash';


const bindings = {
    costs: '<',
    sourceDataRatings: '<'
};


const template = require('./app-costs-section.html');


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


function controller() {
    const vm = this;
    vm.$onChanges = () => {
        vm.currentYear = getCurrentYear(vm.costs);
        vm.currentCosts = filterCostsForYear(vm.currentYear, vm.costs);
        vm.currentTotal = calcTotalCost(vm.currentCosts);
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;
