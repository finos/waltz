
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

import {
    lifecyclePhaseColorScale,
    ragColorScale } from '../../common/colors';


const BINDINGS = {
    applications: '=',
    size: '='
};


const investmentLabels = {
    'R' : 'Disinvest',
    'A' : 'Maintain',
    'G' : 'Invest'
};


const config = {
    colorProvider: (d) => ragColorScale(d.data.key),
    size: 80,
    labelProvider: (k) => investmentLabels[k] || 'Unknown'
};


function calcAppInvestmentPieStats(apps) {
    return _.chain(apps)
        .countBy('overallRating')
        .map((v, k) => ({ key: k, count: v }))
        .value();
}


function controller($scope) {
    const vm = this;

    vm.config = config;
    vm.data = [];

    $scope.$watch('ctrl.size', sz => vm.config.size = sz ? sz : 80);

    $scope.$watch('ctrl.applications', apps => {
        if (!apps) return;
        vm.data = calcAppInvestmentPieStats(apps);
    });

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./apps-by-investment-pie.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
