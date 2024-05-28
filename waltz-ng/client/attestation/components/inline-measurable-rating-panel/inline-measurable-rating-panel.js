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
import {mkTab} from "../../../measurable-rating/measurable-rating-utils";


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

        const viewPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.getViewByCategoryAndAppSelector,
                [vm.measurableCategoryRef.id, mkSelectionOptions(vm.parentEntityRef)],
                {force: true})
            .then(r => r.data);

        const applicationPromise = serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.getById,
                [vm.parentEntityRef.id])
            .then(r => r.data);


        $q.all([applicationPromise, viewPromise])
            .then(([application, viewData]) => {
                vm.ratingInfo = mkTab(viewData, application);
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
