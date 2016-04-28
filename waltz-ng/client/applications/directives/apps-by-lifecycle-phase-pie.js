
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

import {lifecyclePhaseColorScale} from "../../common/colors";
import {toKeyCounts} from "../../common";


const BINDINGS = {
    applications: '=',
    size: '='
};


const DEFAULT_SIZE = 80;

const config = {
    colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
    size: DEFAULT_SIZE
};



function calcAppPhasePieStats(apps) {
    return toKeyCounts(apps, a => a.lifecyclePhase);
}


function controller($scope) {
    const vm = this;

    vm.config = config;
    vm.data = [];

    $scope.$watch('ctrl.size', sz => vm.config.size = sz ? sz : DEFAULT_SIZE);

    $scope.$watch('ctrl.applications', apps => {
        if (!apps) return;
        vm.data = calcAppPhasePieStats(apps);
    });

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./apps-by-lifecycle-phase-pie.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
