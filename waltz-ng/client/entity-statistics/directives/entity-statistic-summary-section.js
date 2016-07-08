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
import {variableScale} from "../../common/colors";


const PIE_SIZE = 70;


const BINDINGS = {
    name: '@',
    entityStatistics: '<'
};


const initData = {
    entityStatistics: [],
    entityStatisticsSorted: [],
};

function mkStatChartData(counts) {
    const byOutcome = {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({ key: c.id, count: c.count }))
            .value()
    };

    return byOutcome;
}


function controller($scope) {
    const vm = _.defaultsDeep(this, initData);

    $scope.$watch('ctrl.entityStatistics', (entityStats = []) => {
        if(!_.isEmpty(entityStats)) {

            _.forEach(entityStats, stat => {
                stat.chart = mkStatChartData(stat.counts);
            });

            vm.entityStatisticsSorted = _.orderBy(entityStats, ['definition.category', 'definition.name']);
        }
    });

}


controller.$inject = ["$scope"];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./entity-statistic-summary-section.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;
