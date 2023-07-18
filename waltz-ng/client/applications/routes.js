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

import { appResolver, appByAssetCodeResolver, orgUnitsResolver } from "./resolvers";

import AppViewAssetCode from "./pages/asset-code-view/app-asset-code-view";
import AppEdit from "./pages/edit/app-edit";
import AppRegistration from "./pages/registration/app-registration";
import AppView from "./pages/view/app-view";
import AppList from "./pages/list/app-list";

const base = {
    url: "application",
    views: {"content@": AppList }
};


const appRegistrationState = {
    url: "/registration",
    views: {"content@": AppRegistration },
};


const appViewState = {
    url: "/{id:int}",
    reloadOnSearch: false,
    views: {
        "content@": AppView
    },
};


const appViewByAssetCodeState = {
    url: "/asset-code/{assetCode}",
    views: {
        "content@": AppViewAssetCode
    },
};


const appViewByExternalIdState = {
    url: "/external-id/{assetCode}",
    views: {
        "content@": AppViewAssetCode
    }
};


const appEditState = {
    url: "/{id:int}/edit",
    resolve: {
        app: appResolver,
        orgUnits: orgUnitsResolver
    },
    views: {"content@": AppEdit},
};


function setup($stateProvider) {
    $stateProvider
        .state("main.app", base)
        .state("main.app.registration", appRegistrationState)
        .state("main.app.view", appViewState)
        .state("main.app.asset-code", appViewByAssetCodeState)
        .state("main.app.external-id", appViewByExternalIdState)
        .state("main.app.edit", appEditState);
}


setup.$inject = [
    "$stateProvider"
];


export default setup;