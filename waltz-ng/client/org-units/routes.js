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

import {appTalliesResolver, endUserAppTalliesResolver, orgUnitsResolver} from "./resolvers.js";

import HomeView from "./pages/home-view/home-view";
import UnitView from "./pages/unit-view/unit-view";


const baseState = {
};

const homeState = {
    url: "org-units",
    views: {"content@": HomeView},
    resolve: {
        appTallies: appTalliesResolver,
        endUserAppTallies: endUserAppTalliesResolver,
        orgUnits: orgUnitsResolver
    }
};


const viewState = {
    url: "org-units/{id:int}",
    views: {
        "content@": UnitView
    },
};


function setup($stateProvider) {
    $stateProvider
        .state("main.org-unit", baseState)
        .state("main.org-unit.list", homeState)
        .state("main.org-unit.view", viewState);
}

setup.$inject = ["$stateProvider"];


export default setup;