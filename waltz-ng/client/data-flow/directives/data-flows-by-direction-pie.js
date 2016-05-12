
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

import {flowDirectionColorScale} from "../../common/colors";


const BINDINGS = {
    stats: '=',
    size: '='
};


function toPieStats(stats) {
    return [
        { key: "Inbound", count: stats.upstreamApplicationCount },
        { key: "Outbound", count: stats.downstreamApplicationCount },
        { key: "Intra", count: stats.intraApplicationCount }
    ];
}

const DEFAULT_SIZE = 80;


const config = {
    colorProvider: (d) => flowDirectionColorScale(d.data.key),
    size: DEFAULT_SIZE
};


function controller($scope) {
    const vm = this;

    vm.config = config;
    vm.data = [];

    $scope.$watch('ctrl.size', sz => vm.config.size = sz ? sz : DEFAULT_SIZE);

    $scope.$watch('ctrl.stats', (stats) => {
        if (!stats) return;
        vm.data = toPieStats(stats);
    });

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./data-flows-by-direction-pie.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
