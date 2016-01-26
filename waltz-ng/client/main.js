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

import '../style/style.scss';

import _ from 'lodash';
import angular from 'angular';

import 'angular-ui-grid/ui-grid';
import 'angular-ui-router';
import 'angular-ui-bootstrap';
import 'ui-select';
import 'satellizer';
import 'angular-animate';
import 'angular-sanitize';
import 'ng-tags-input';
import 'babel-core/polyfill';  // provides Object.assign etc.
import 'angular-local-storage';
import 'ng-redux';
import thunk from 'redux-thunk';

import rootReducer from './reports/reducers';


const dependencies = [
    'ui.bootstrap',
    'ui.router',
    'ui.select',
    'ui.grid',
    'ngAnimate',
    'ngSanitize',
    'ngTagsInput',
    'satellizer',
    'LocalStorageModule',
    'ngRedux',
    require('angular-formly'),
    require('angular-formly-templates-bootstrap')
];


const waltzApp = angular.module('waltz-app', dependencies);


const registrationFns = [
    require('./common/directives'),
    require('./common/filters'),
    require('./common/services'),
    require('./access-log'),
    require('./applications'),
    require('./app-capabilities'),
    require('./app-view'),
    require('./asset-cost'),
    require('./auth-sources'),
    require('./bookmarks'),
    require('./capabilities'),
    require('./complexity'),
    require('./change-log'),
    require('./data-flow'),
    require('./data-types'),
    require('./end-user-apps'),
    require('./examples'),
    require('./history'),
    require('./involvement'),
    require('./navbar'),
    require('./org-units'),
    require('./perspectives'),
    require('./person'),
    require('./playpen'),
    require('./ratings'),
    require('./server-info'),
    require('./sidebar'),
    require('./svg-diagram'),
    require('./user'),
    require('./formly'),
    require('./widgets'),
    require('./reports')
];


_.each(registrationFns, (registrationFn, idx) => {
    if (!_.isFunction(registrationFn)) {
        console.error('cannot register: ', registrationFn, 'at idx', idx);
    }
    registrationFn(waltzApp);
});


waltzApp.config([
    '$stateProvider',
    ($stateProvider) => {
        $stateProvider
            .state('main', {
                url: '/',
                views: {
                    'header': { template: '<waltz-navbar></waltz-navbar>'},
                    'sidebar': { template: '<waltz-sidebar></waltz-sidebar>' },
                    'content': { template: require('./welcome/welcome.html') },
                    'footer': { template: require('./footer/footer.html') }
                }
            })
            .state('main.home', {
                url: 'home',
                views: {
                    'content': { template: require('./welcome/welcome.html') }
                }
            });
    }
]);


const baseUrl =
    __ENV__ === 'prod'
    ? './'
    : __ENV__ === 'test'
        ? 'http://192.168.1.147:8443/'
        : 'http://localhost:8443/';

waltzApp.constant('BaseApiUrl', baseUrl + 'api');
waltzApp.constant('BaseUrl', baseUrl);
waltzApp.constant('BaseExtractUrl', baseUrl + 'data-extract');


waltzApp.config([
    'uiSelectConfig',
    (uiSelectConfig) => {
        uiSelectConfig.theme = 'bootstrap';
        uiSelectConfig.resetSearchInput = true;
        uiSelectConfig.appendToBody = true;
    }
]);


waltzApp.config( [
    '$compileProvider',
    $compileProvider => {
        $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|sip|chrome-extension):/);
    }
]);

waltzApp.config([
    '$authProvider',
    'BaseUrl',
    function($authProvider, BaseUrl) {
        $authProvider.baseUrl = BaseUrl;
        $authProvider.withCredentials = false;

        $authProvider.google({
            clientId: 'Google account'
        });

        $authProvider.github({
            clientId: 'GitHub Client ID'
        });

        $authProvider.linkedin({
            clientId: 'LinkedIn Client ID'
        });

    }
]);


waltzApp.config([
    '$httpProvider',
    function($httpProvider) {
        $httpProvider.defaults.cache = false;
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }
        // disable IE ajax request caching
        $httpProvider.defaults.headers.get['If-Modified-Since'] = '0';
    }
]);

waltzApp.run(['$rootScope', '$document', ($rootScope, $doc) => {
    $rootScope.$on('$stateChangeSuccess', () => {
        $doc[0].body.scrollTop = 0;
        $doc[0].documentElement.scrollTop = 0;
    });
}]);

waltzApp.config(['$ngReduxProvider', ($ngReduxProvider) => {
    $ngReduxProvider.createStoreWith(rootReducer, [thunk], []);
}]);