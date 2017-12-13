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

import template from './app-asset-code-view.html';


function controller($state,
                    $stateParams,
                    resolvedAppsByAssetCode) {

    const vm = this;

    vm.resolvedAppsByAssetCode = resolvedAppsByAssetCode || [];
    vm.assetCode = $stateParams.assetCode;

    const goToApp = app => $state.go('main.app.view', { id: app.id }, { location: false });

    // if single app for asset code, navigate to the app now
    if (vm.resolvedAppsByAssetCode.length == 1) {
        goToApp(resolvedAppsByAssetCode[0]);
    }
}


controller.$inject = [
    '$state',
    '$stateParams',
    'resolvedAppsByAssetCode'
];


export default  {
    template,
    controller,
    controllerAs: 'ctrl'
};

