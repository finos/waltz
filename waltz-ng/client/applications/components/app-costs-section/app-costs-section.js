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

import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common/index";
import template from "./app-costs-section.html";


const bindings = {
    parentEntityRef: "<"
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
            .map("cost.year")
            .max()
            .value();
    return foundYear || defaultYear;
}


function calcTotalCost(costs = []) {
    return _.sumBy(costs, "cost.amount").toFixed(2);
}


function filterCostsForYear(year, costs = []) {
    return _.chain(costs)
        .filter(c => c.cost.year === year)
        .sortBy(c => c.cost.costKind)
        .value();
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
                vm.costs = _.orderBy(r.data, ['cost.year'], ['desc']);
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
    "ServiceBroker",
];


const component = {
    bindings,
    template,
    controller
};

export default {
    component,
    id: "waltzAppCostsSection"
};
