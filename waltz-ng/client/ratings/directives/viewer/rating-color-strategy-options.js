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

import { selectBest, selectWorst, mkSelectByMeasure } from './coloring-strategies';

const BINDINGS = {
    selectedStrategy: '='
};


function controller(perspectiveStore) {

    const vm = this;

    vm.strategies = [
        selectBest,
        selectWorst
    ];

    perspectiveStore.findByCode('BUSINESS')
        .then(p => p.measurables)
        .then(measurables => _.map(measurables, m => mkSelectByMeasure(m)))
        .then(strategies => _.each(strategies, s => vm.strategies.push(s)));

}

controller.$inject = ['PerspectiveStore'];


export default () => ({
    replace: true,
    restrict: 'E',
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS,
    template: '<span>\n    <select ng-model="ctrl.selectedStrategy" ng-options="option.name for option in ctrl.strategies"></select>\n    <span class="small" ng-bind="ctrl.selectedStrategy.description"></span>\n</span>'
});

