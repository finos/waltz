/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import angular from "angular";

import "../style/style.scss";


const waltzApp = angular.module('waltz.app', require('./modules'));


if (__ENV__ === 'prod') {
    waltzApp.config(['$compileProvider', function ($compileProvider) {
        $compileProvider.debugInfoEnabled(false);
        console.log("debug disabled, re-enable with:", "angular.reloadWithDebugInfo();");
    }]);
}

require('./routes')(waltzApp);
require('./networking')(waltzApp);
require('./thirdparty-setup')(waltzApp);


function hrefSanitizer($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(mailto|https?|sip|chrome-extension):/);
}

hrefSanitizer.$inject = ['$compileProvider'];


waltzApp.config(hrefSanitizer);


waltzApp.run([
    'UserAgentInfoStore',
    (userAgentStore) => userAgentStore.save()
]);
