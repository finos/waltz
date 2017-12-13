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

import template from './currency-amount.html';
import {initialiseData} from "../common/index";
import namedSettings from "../system/named-settings";
import {currenciesByCode} from "../common/currency-utils";
import {numberFormatter} from "../common/string-utils";

const bindings = {
    currencyCode: '<?',
    amount: '<',
    simplify: '@?'
};


const initialState = {
    currencyStr: '-',
    simplify: false

};


function controller($filter, settingsService) {
    const vm = initialiseData(this, initialState);

    let currency = null;

    const refresh = () => {
        if (currency && vm.amount) {
            if (vm.simplify) {
                vm.currencyStr = currency.symbol + numberFormatter(vm.amount, 1, true);
            } else {
                vm.currencyStr = $filter('currency')(vm.amount, currency.symbol, currency.fraction)
            }
        }
    };

    vm.$onChanges = refresh;

    vm.$onInit = () => {
        settingsService
            .findOrDefault(namedSettings.defaultCurrency, 'EUR')
            .then(dfltCode => currency = currenciesByCode[vm.currencyCode || dfltCode])
            .then(refresh);
    };
}


controller.$inject = [
    '$filter',
    'SettingsService'
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id : 'waltzCurrencyAmount'
};