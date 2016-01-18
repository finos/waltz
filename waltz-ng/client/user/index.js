
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
import userManagementView from './user-management';
import userLogView from './user-log';

const userManagementState = {
    url: 'user/management',
    views: {'content@': userManagementView }
};


const userLogState = {
    url: 'user/log',
    views: {'content@': userLogView }
};


function configureStates(stateProvider) {
    stateProvider
        .state('main.user', {})
        .state('main.user.management', userManagementState)
        .state('main.user.log', userLogState);
}

configureStates.$inject = ['$stateProvider'];


export default (module) => {
    module
        .directive('waltzHasRole', require('./directives/has-role'))
        .directive('waltzUnlessRole', require('./directives/unless-role'))
        .service('UserService', require('./services/user-service'))
        .service('UserStore', require('./services/user-store'))
        .config(configureStates);
};
