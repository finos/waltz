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
    template: require('./app-asset-code-view.html'),
    controller,
    controllerAs: 'ctrl'
};

