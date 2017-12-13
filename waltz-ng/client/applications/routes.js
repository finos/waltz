/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { appResolver, appByAssetCodeResolver, orgUnitsResolver } from "./resolvers";

import AppViewAssetCode from "./pages/asset-code-view/app-asset-code-view";
import AppEdit from "./pages/edit/app-edit";
import AppRegistration from "./pages/registration/app-registration";
import AppView from "./pages/view/app-view";


const base = {
    url: 'application'
};


const appRegistrationState = {
    url: '/registration',
    views: {'content@': AppRegistration }
};


const appViewState = {
    url: '/{id:int}?{sections:string}',
    reloadOnSearch: false,
    views: {
        'content@': AppView
    }
};


const appViewByAssetCodeState = {
    url: '/asset-code/{assetCode}',
    views: {
        'content@': AppViewAssetCode
    },
    resolve: { resolvedAppsByAssetCode: appByAssetCodeResolver }
};


const appEditState = {
    url: '/{id:int}/edit',
    resolve: {
        app: appResolver,
        orgUnits: orgUnitsResolver
    },
    views: {'content@': AppEdit}
};


function setup($stateProvider) {
    $stateProvider
        .state('main.app', base)
        .state('main.app.registration', appRegistrationState)
        .state('main.app.view', appViewState)
        .state('main.app.asset-code', appViewByAssetCodeState)
        .state('main.app.edit', appEditState)
}


setup.$inject = [
    '$stateProvider'
];


export default setup;