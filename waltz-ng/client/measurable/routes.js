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
import UnitView from "./pages/measurable-view/measurable-view";

import {CORE_API} from "../common/services/core-api-utils";


const baseState = {};


const viewState = {
    url: "measurable/{id:int}",
    views: {
        "content@": UnitView
    }
};


function bouncer($q, $state, $stateParams, serviceBroker) {
    const categoryExternalId = $stateParams.category;
    const externalId = $stateParams.externalId;

    $q.all([serviceBroker.loadAppData(CORE_API.MeasurableCategoryStore.findAll),
            serviceBroker.loadViewData(CORE_API.MeasurableStore.findByExternalId, [externalId])])
        .then(([categoriesResult, measurablesResult]) => {

            const categories = categoriesResult.data;
            const measurables = measurablesResult.data;
            const category = _.find(categories, {externalId: categoryExternalId});

            if(category) {
                const m = _.find(measurables, {categoryId: category.id});
                if (m) {
                    $state.go("main.measurable.view", {id: m.id});
                } else {
                    console.log(`Cannot find measure corresponding category: ${categoryExternalId}, external id: ${externalId}`);
                }
            } else {
                console.log(`Cannot find measure corresponding category: ${categoryExternalId}, external id: ${externalId}`);
            }
        });
}


bouncer.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


const bouncerState = {
    url: "measurable/external-id/{category:string}/{externalId:string}",
    resolve: {
        bouncer
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.measurableBouncer", bouncerState)
        .state("main.measurable", baseState)
        .state("main.measurable.view", viewState);
}


setup.$inject = ["$stateProvider"];


export default setup;