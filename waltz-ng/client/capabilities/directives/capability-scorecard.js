/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import {enrichServerStats} from "../../server-info/services/server-utilities";
import {calcPortfolioCost} from "../../asset-cost/services/asset-cost-utilities";
import {calcComplexitySummary} from "../../complexity/services/complexity-utilities";


const BINDINGS = {
    capability: '=',
    applications: '=',
    assetCosts: '=',
    complexity: '=',
    serverStats: '=',
    flows: '=',
    traits: '='
};



function controller($scope, $state) {

    const vm = this;

    $scope.$watch('ctrl.assetCosts', cs => vm.portfolioCostStr = calcPortfolioCost(cs));
    $scope.$watch('ctrl.complexity', cs => vm.complexitySummary = calcComplexitySummary(cs));
    $scope.$watch('ctrl.serverStats', enrichServerStats);

    vm.subCapabilitySelected = () => $state.go('main.capability.view', { id: vm.selectedSubCapability} );
}

controller.$inject = ['$scope', '$state'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./capability-scorecard.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
