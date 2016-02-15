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

import { numberFormatter } from '../../common';


const BINDINGS = {
    person: '=',
    directs: '=',
    managers: '=',
    applications: '=',
    assetCosts: '=',
    complexity: '=',
    serverStats: '='
};


function controller($scope) {

    const vm = this;

    function recalcPortfolioCosts(costs) {
        if (!costs) return;
        const amount = _.sum(costs, 'cost.amount');

        vm.portfolioCostStr = '€ ' + numberFormatter(amount, 1);
    }


    function recalcComplexity(complexity) {
        if (!complexity) return;
        const cumulativeScore = _.sum(complexity, "overallScore");
        const averageScore = complexity.length > 0 ? cumulativeScore / complexity.length : 0;
        vm.complexitySummary =  {
            cumulativeScore,
            averageScore
        };
    }


    function recalcServerStats(serverStats) {
        if (! serverStats) return;
        serverStats.total = serverStats.physicalCount + serverStats.virtualCount;

        serverStats.virtualPercentage = serverStats.total > 0
            ? Number((serverStats.virtualCount / serverStats.total) * 100).toFixed(1)
            : "-";

        vm.serverStats = serverStats;
    }


    $scope.$watch('ctrl.assetCosts', recalcPortfolioCosts);
    $scope.$watch('ctrl.complexity', recalcComplexity);
    $scope.$watch('ctrl.serverStats', recalcServerStats);

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./person-summary.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};