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

import AppEdit from "./app-edit";
import AppRegistration from "./app-registration";
import appTagExplorerView from "./app-tag-explorer";
import {appResolver, appByAssetCodeResolver, tagsResolver, aliasesResolver, orgUnitsResolver} from "./resolvers";


const base = {
    url: 'application'
};


const appRegistrationState = {
    url: '/registration',
    views: {'content@': AppRegistration }
};


const appViewState = {
    url: '/{id:int}',
    views: {
        'content@': require('./app-view')
    }
};


const appViewByAssetCodeState = {
    url: '/asset-code/{assetCode}',
    views: {
        'content@': require('./app-asset-code-view')
    },
    resolve: { resolvedAppsByAssetCode: appByAssetCodeResolver }
};


const appEditState = {
    url: '/{id:int}/edit',
    resolve: {
        app: appResolver,
        tags: tagsResolver,
        aliases: aliasesResolver,
        orgUnits: orgUnitsResolver
    },
    views: {'content@': AppEdit}
};


const appTagExplorerState = {
    url: 'tag-explorer/:tag',
    views: {'content@': appTagExplorerView }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.app', base)
        .state('main.app.registration', appRegistrationState)
        .state('main.app.view', appViewState)
        .state('main.app.asset-code', appViewByAssetCodeState)
        .state('main.app.edit', appEditState)
        .state('main.app.tag-explorer', appTagExplorerState);
}


setup.$inject = [
    '$stateProvider'
];


export default setup;