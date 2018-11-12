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