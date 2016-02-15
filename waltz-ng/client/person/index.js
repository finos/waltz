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


import PersonView from './person-view';
import PersonHome from './person-home';


export default (module) => {

    require('./services')(module);

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.person', {
                    url: 'person',
                    views: {'content@': PersonHome }
                })
                .state('main.person.view', {
                    url: '/:empId',
                    views: {'content@': PersonView }
                });
        }
    ]);

    module.directive('waltzPersonSelector', require('./directives/person-selector'));
    module.directive('waltzPersonLink', require('./directives/person-link'));
    module.directive('waltzManagerList', require('./directives/manager-list'));
    module.directive('waltzPersonDirectsList', require('./directives/person-directs-list'));
    module.directive('waltzPersonSummary', require('./directives/person-summary'));

    module.service('PersonViewDataService', require('./person-view-data'));

};
