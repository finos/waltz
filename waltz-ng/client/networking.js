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
import namedSettings from "./system/named-settings";

function run($http, settingsService) {

    settingsService
        .isDevModeEnabled()
        .then(devModeEnabled => {
            if (devModeEnabled) {

                settingsService.findAll()
                    .then(settings => {

                        console.log('Dev Extensions enabled');
                        _.chain(settings)
                            .filter(s => s.name.startsWith(namedSettings.httpHeaderPrefix))
                            .each(s => {
                                const headerName = s.name.replace(namedSettings.httpHeaderPrefix, '');
                                $http.defaults.headers.common[headerName] = s.value;
                            })
                            .value()

                    })

            }
        });

}

run.$inject = [
    '$http',
    'SettingsService'
];


function configure($httpProvider) {

    $httpProvider.defaults.cache = false;

    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }

    // disable IE ajax request caching
    $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';

    // using apply async should improve performance
    $httpProvider.useApplyAsync(true);
}

configure.$inject = [
    '$httpProvider'
];


function setupNetworking(module) {
    const baseUrl =
        __ENV__ === 'prod'
            ? './'
            : __ENV__ === 'test'
            ? 'http://10.217.34.239:8443/'
            : 'http://localhost:8443/';
    //: 'http://192.168.1.147:8443/'  // TODO (if testing IE on Mac) : use ip lookup

    module
        .constant('BaseApiUrl', baseUrl + 'api')
        .constant('BaseUrl', baseUrl)
        .constant('BaseExtractUrl', baseUrl + 'data-extract')
        .config(configure)
        .run(run);
}



export default setupNetworking;
