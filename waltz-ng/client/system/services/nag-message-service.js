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
