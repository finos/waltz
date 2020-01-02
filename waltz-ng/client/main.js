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

import angular from "angular";

import "../style/style.scss";
import Modules from './modules';
import Routes from './routes';
import Networking from './networking';
import ThirdpartySetup from './thirdparty-setup';



const waltzApp = angular.module('waltz.app', Modules);


if (__ENV__ === 'prod') {
    waltzApp.config(['$compileProvider', function ($compileProvider) {
        $compileProvider.debugInfoEnabled(false);
        console.log("debug disabled, re-enable with:", "angular.reloadWithDebugInfo();");
    }]);
}

Routes(waltzApp);
Networking(waltzApp);
ThirdpartySetup(waltzApp);


function hrefSanitizer($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(mailto|https?|sip|chrome-extension):/);
}
hrefSanitizer.$inject = ['$compileProvider'];

waltzApp.config(hrefSanitizer);


waltzApp.run([
    'UserAgentInfoStore',
    (userAgentStore) => userAgentStore.save()
]);
