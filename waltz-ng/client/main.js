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

import "../style/style.scss";
import angular from "angular";
import "angular-ui-notification";
import "angular-ui-grid/ui-grid";
import "angular-ui-router";
import "angular-ui-bootstrap";
import "angular-tree-control";
import "ui-select";
import "satellizer";
import "angular-animate";
import "angular-sanitize";
import "ng-tags-input";
import "babel-core/polyfill";
import "angular-local-storage";
import "ng-redux";


const dependencies = [
    'ui.bootstrap',
    'ui.router',
    'ui.select',
    'ui.grid',
    'ui.grid.exporter',
    'ui.grid.resizeColumns',
    'ui-notification',
    'ngAnimate',
    'ngSanitize',
    'ngTagsInput',
    'satellizer',
    'LocalStorageModule',
    'ngRedux',
    require('angular-formly'),
    require('angular-formly-templates-bootstrap'),
    'treeControl'
];


const waltzApp = angular.module('waltz-app', dependencies);

if (__ENV__ === 'prod') {
    waltzApp.config(['$compileProvider', function ($compileProvider) {
        $compileProvider.debugInfoEnabled(false);
        console.log("debug disabled, re-enable with:", "angular.reloadWithDebugInfo();");
    }]);
}


require('./modules')(waltzApp);
require('./thirdparty-setup')(waltzApp);


waltzApp.config( [
    '$compileProvider',
    $compileProvider => {
        $compileProvider.aHrefSanitizationWhitelist(/^\s*(mailto|https?|sip|chrome-extension):/);
    }
]);


waltzApp.run([
    'UserAgentInfoStore',
    (userAgentStore) =>   userAgentStore.save()
]);


