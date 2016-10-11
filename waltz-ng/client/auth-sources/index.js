
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

import {
    authSourcesResolver,
    flowResolver,
    idResolver,
    orgUnitsResolver,
    dataTypesResolver,
    flowDecoratorsResolver
} from "./resolvers";
import editView from "./edit";


export default (module) => {

    require('./directives')(module);

    module
        .service('AuthSourcesStore', require('./services/auth-sources-store'))
        .service('AuthSourcesCalculator', require('./services/auth-sources-calculator'));

    module
        .component('waltzAuthSourcesList', require('./components/auth-sources-list'))
        .component('waltzNonAuthSourcesList', require('./components/non-auth-sources-list'));

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.auth-sources', {
                    url: 'auth-sources'
                })
                .state('main.auth-sources.edit', {
                    url: '/:kind/{id:int}/edit',
                    views: { 'content@': editView },
                    resolve: {
                        authSources: authSourcesResolver,
                        orgUnits: orgUnitsResolver,
                        id: idResolver,
                        flows: flowResolver,
                        flowDecorators: flowDecoratorsResolver,
                        dataTypes: dataTypesResolver
                    }
                });
        }
    ]);
};
