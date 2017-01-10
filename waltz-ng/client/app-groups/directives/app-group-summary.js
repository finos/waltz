/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {enrichServerStats} from "../../server-info/services/server-utilities";
import {calcPortfolioCost} from "../../asset-cost/services/asset-cost-utilities";
import {calcComplexitySummary} from "../../complexity/services/complexity-utilities";


const BINDINGS = {
    appGroup: '=',
    applications: '=',
    totalCost: '=',
    complexity: '=',
    serverStats: '=',
    editable: '=',
    flows: '='
};


function controller($scope) {

    const vm = this;

    $scope.$watch('ctrl.totalCost', cs => vm.portfolioCostStr = calcPortfolioCost(cs));
    $scope.$watch('ctrl.complexity', cs => vm.complexitySummary = calcComplexitySummary(cs));
    $scope.$watch('ctrl.serverStats', stats => vm.enrichedServerStats = enrichServerStats(stats));

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./app-group-summary.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
