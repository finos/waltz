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

function uiSelectSetup(uiSelectConfig) {
    uiSelectConfig.theme = "bootstrap";
    uiSelectConfig.resetSearchInput = true;
    uiSelectConfig.appendToBody = true;
}

uiSelectSetup.$inject = ["uiSelectConfig"];


function authProviderSetup($authProvider, BaseUrl) {
    $authProvider.baseUrl = BaseUrl;
    $authProvider.loginUrl = "/authentication/login";
    $authProvider.withCredentials = false;

    $authProvider.google({
        clientId: "Google account"
    });


    $authProvider.github({
        clientId: "c39a1ce93114bb52ab06",
        url: '/authentication/oauth',
        authorizationEndpoint: 'https://github.com/login/oauth/authorize',
        redirectUri: window.location.origin + "/authentication/login",
        optionalUrlParams: ['scope'],
        scope: ['user:email'],
        scopeDelimiter: ' ',
        oauthType: '2.0',
        popupOptions: { width: 1020, height: 618 }
    });

    $authProvider.oauth2({
        name: "demo-provider",
        clientId: "c39a1u5u514bb52ab06",
        url: '/authentication/oauth',
        authorizationEndpoint: 'https://waltz.com/login/oauth/authorize',
        redirectUri: window.location.origin + "/authentication/login",
        optionalUrlParams: ['scope'],
        scope: ['user:email'],
        scopeDelimiter: ' ',
        oauthType: '2.0',
        popupOptions: { width: 1020, height: 618 }
    });
}

authProviderSetup.$inject = [
    "$authProvider",
    "BaseUrl",
];


function setup(module) {
    module
        .config(uiSelectSetup)
        .config(authProviderSetup);

    // for formly setup see: `formly/index.js`
}


export default (module) => setup(module);
