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