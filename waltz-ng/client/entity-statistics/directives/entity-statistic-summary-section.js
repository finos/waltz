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


const BINDINGS = {
    name: '@',
    definitions: '<',
    summaries: '<',
    parentRef: '<'
};


const initData = {
    definitions: [],
    summaries: []
};


function controller($scope) {
    const vm = _.defaultsDeep(this, initData);

    $scope.$watch('ctrl.summaries', (summaries = []) => {
        if(!_.isEmpty(summaries)) {
            vm.summariesByDefinitionId = _.keyBy(vm.summaries, 'entityReference.id');
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
