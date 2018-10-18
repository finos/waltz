/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
                console.log("got data: ", {parentRef: vm.parentEntityRef, cat: vm.measurableCategory, vm} );
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
