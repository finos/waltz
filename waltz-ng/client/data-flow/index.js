
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

import RegistrationView from './registration-view';


export default (module) => {

    require('./directives')(module);

    module.service('DataFlowDataStore', require('./services/data-flow-store'));
    module.service('RatedDataFlowDataService', require('./services/rated-data-flow-service'));
    module.service('DataFlowUtilityService', require('./services/data-flow-utility'));


    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.data-flow', {
                    url: 'data-flow'
                })
                .state('main.data-flow.registration', {
                    url: '/registration/:id',
                    views: {'content@': RegistrationView },
                    resolve: {
                        application: [
                            'ApplicationStore',
                            '$stateParams',
                            (ApplicationStore, $stateParams) => ApplicationStore.getById($stateParams.id)
                        ]
                    }
                });
        }
    ]);

};
