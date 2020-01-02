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

import namedSettings from "../named-settings";


function hasRole(userService, role) {
    return userService
        .whoami()
        .then(user => userService.hasRole(user, role));
}


function isBetaServer(settingsService) {

    return settingsService
        .findOrDefault(namedSettings.betaEnvironment, false)
        .then(isBeta => { return isBeta === 'true'; });
}


function getNagEnabled($q, userService, settingsService) {

    return $q.all( [ hasRole(userService, 'BETA_TESTER'), isBetaServer(settingsService) ] )
        .then( ([isBetaTester, isBetaServer]) => !isBetaTester && isBetaServer) ;
}


function getNagMessage(settingsService) {
    return settingsService
        .findOrDefault(namedSettings.betaNagMessage, "You are using a test server, data will not be preserved");
}


function service($q, settingsService, userService) {

    const setupNag = (nagFunction) => {
        getNagEnabled($q, userService, settingsService)
            .then(nagEnabled => {
                if(nagEnabled) {
                    getNagMessage(settingsService)
                        .then(nagMessage => nagFunction(nagMessage));
                }
            });
    };


    return {
        setupNag
    };
}


service.$inject = [
    '$q',
    'SettingsService',
    'UserService'
];


export default service;
