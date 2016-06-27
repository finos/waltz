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
import _ from "lodash";
import d3 from "d3";

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

            vm.entityStatisticsGrouped = d3.nest()
                .key(x => x.definition.category)
                .sortKeys(d3.ascending)
                .sortValues((a, b) => a.definition.name < b.definition.name)
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
