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

import _ from 'lodash';
import d3 from 'd3';
import angular from 'angular';


function controller($state, $window, $location, complexityStore) {
    const vm = this;
    complexityStore.findByOrgUnitTree(230).then(c => vm.complexity = c);
}

controller.$inject = [
    '$state', '$window', '$location', 'ComplexityStore'
];


const playpenView = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default (module) => {

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.playpen', {
                    url: 'playpen',
                    views: { 'content@': playpenView }
                });
        }
    ]);

    module.directive("waltzComplexityBarChart", require('./complexity-bar-chart'));
};
