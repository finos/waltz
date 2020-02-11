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
import {initialiseData} from "../../../common";

import template from "./inline-measurable-rating-panel.html";


const bindings = {
    parentEntityRef: "<",
    measurableCategoryRef: "<",
};


const initialState = {
    categories: [],
    measurableCategory: {},
    ratings: [],
    ratingSchemesById: {},
    measurables: []
};


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadData = (force = false) => {

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRatingStore.findForEntityReference, [ vm.parentEntityRef ], { force })
            .then(r => vm.ratings = r.data);

        const ratingSchemesPromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemesById = _.keyBy(r.data, "id"));

        const categoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.getById, [vm.measurableCategoryRef.id])
            .then(r => vm.measurableCategory = r.data);

        const measurablesPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesRelatedToPath, [vm.parentEntityRef], { force })
            .then(r => vm.measurables = _.filter(r.data, m => m.categoryId === vm.measurableCategoryRef.id));

        $q.all([measurablesPromise, ratingSchemesPromise, ratingsPromise, categoriesPromise])
            .then(() => {
                vm.ratingScheme = vm.ratingSchemesById[vm.measurableCategory.ratingSchemeId];
            });
    };

    vm.$onInit = () => loadData();

    vm.$onChanges = (changes) => {
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzInlineMeasurableRatingPanel"
};
