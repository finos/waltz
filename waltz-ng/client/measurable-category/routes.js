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
import ListView from "./pages/list/measurable-category-list.js";
import EditView from "./pages/edit/measurable-category-edit.js";
import {lastViewedMeasurableCategoryKey} from "../user";
import {CORE_API} from "../common/services/core-api-utils";


const baseState = {};


function bouncer($state, $stateParams, settingsService, serviceBroker) {
    const go = id => $state.go("main.measurable-category.list", { id }, { location: "replace" });

    const attemptToRouteViaLastVisited = () => serviceBroker
        .loadViewData(CORE_API.UserPreferenceStore.findAllForUser, [], {force: true})
        .then(prefs => {
            const lastCategory = _.find(prefs.data, p => p.key === lastViewedMeasurableCategoryKey);
            if (_.get(lastCategory, ["value"], 0) > 0) {
                go(lastCategory.value);
            } else {
                attemptToRouteViaServerSetting();
            }
        });

    const attemptToRouteViaServerSetting = () => settingsService
        .findOrDefault("settings.measurable.default-category", null)
        .then(defaultCategoryId => {
            if (defaultCategoryId) {
                go(defaultCategoryId)
            } else {
                go(1);
            }
        });

    attemptToRouteViaLastVisited();
}

bouncer.$inject = ["$state", "$stateParams", "SettingsService", "ServiceBroker"];


const bouncerState = {
    url: "measurable-category/",
    resolve: {
        bouncer
    }
};

const listState = {
    url: "measurable-category/{id:int}",
    views: {
        "content@": ListView
    }
};

const editState = {
    url: "measurable-category/{id:int}/edit",
    views: {
        "content@": EditView
    }
};


function setup($stateProvider) {
    $stateProvider
        .state("main.measurable-category", baseState)
        .state("main.measurable-category.index", bouncerState)
        .state("main.measurable-category.list", listState)
        .state("main.measurable-category.edit", editState);
}


setup.$inject = [
    "$stateProvider"
];


export default setup;