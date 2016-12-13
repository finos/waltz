/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {ascending} from "d3-array";
import {nest} from "d3-collection";

const BINDINGS = {
    name: '@',
    entityStatistics: '<'
};


const initData = {
    entityStatistics: [],
    entityStatisticsGrouped: []
};


function controller($scope) {
    const vm = _.defaultsDeep(this, initData);

    $scope.$watch('ctrl.entityStatistics', (entityStats = []) => {
        if(!_.isEmpty(entityStats)) {

            vm.entityStatisticsGrouped = nest()
                .key(x => x.definition.category)
                .sortKeys(ascending)
                .key(x => x.definition.name)
                .sortKeys(ascending)
                .sortValues((a, b) => a.value.outcome < b.value.outcome)
                .entries(entityStats);
        }
    });

}


controller.$inject = ["$scope"];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./entity-statistic-section.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;
