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

import {initialiseData} from "../../../common";

import template from "./inline-measurable-rating-panel.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";


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

    const loadData = () => {
        const measurablesPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableStore.findMeasurablesBySelector,
                [mkSelectionOptions(vm.parentEntityRef)])
            .then(r => r.data);

        const measurableRatingsPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.findForEntityReference,
                [vm.parentEntityRef],
                {force: true})
            .then(r => r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(
                CORE_API.RatingSchemeStore.findRatingsForEntityAndMeasurableCategory,
                [vm.parentEntityRef, vm.measurableCategoryRef.id])
            .then(r => r.data);

        $q.all([measurablesPromise, measurableRatingsPromise, ratingsPromise])
            .then(([measurables, measurableRatings, ratingSchemeItems]) => {
                vm.measurables = _.filter(measurables, d => d.categoryId === vm.measurableCategoryRef.id);
                vm.ratings = measurableRatings;
                vm.ratingSchemeItems = ratingSchemeItems;
            });
    };

    vm.$onChanges = (changes) => {
        if(vm.parentEntityRef && vm.measurableCategoryRef){
            loadData();
        }
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
