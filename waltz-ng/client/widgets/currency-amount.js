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
        if (currency && vm.amount != null) {
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