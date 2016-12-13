
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

import angular from 'angular';

import {
    authSourcesResolver,
    flowResolver,
    idResolver,
    orgUnitsResolver,
    dataTypesResolver,
    flowDecoratorsResolver
} from "./resolvers";
import editView from "./edit";


export default () => {

    const module = angular.module('waltz.auth.sources', []);

    require('./directives')(module);

    module
        .service('AuthSourcesStore', require('./services/auth-sources-store'))
        .service('AuthSourcesCalculator', require('./services/auth-sources-calculator'));

    module
        .component('waltzAuthSourcesList', require('./components/auth-sources-list'))
        .component('waltzNonAuthSourcesList', require('./components/non-auth-sources-list'))
        .component('waltzAuthSourcesTable', require('./components/auth-sources-table'));

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

    return module.name;
};
