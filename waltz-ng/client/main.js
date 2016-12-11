/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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
