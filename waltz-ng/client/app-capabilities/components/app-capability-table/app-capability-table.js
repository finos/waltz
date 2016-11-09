
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



function controller($scope) {
    const vm = this;

    const refresh = ([appCapabilities, capabilities]) => {
        if (!appCapabilities || !capabilities) return;

        const capabilitiesById = _.keyBy(capabilities, 'id');

        vm.items = _.map(appCapabilities, ac => {
            return {
                ...ac,
                capability: capabilitiesById[ac.capabilityId]
            }
        });
    };

    $scope.$watchGroup(['ctrl.appCapabilities', 'ctrl.capabilities'], refresh)
}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./app-capability-table.html'),
        scope: {},
        controller,
        controllerAs: 'ctrl',
        bindToController: {
            appCapabilities: '=',
            capabilities: '='
        }
    };
};
