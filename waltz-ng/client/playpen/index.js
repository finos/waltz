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




function controller($state, $window, $location, $anchorScroll) {
    const vm = this;

    vm.jumpToWin = (id) => {
        console.log($window.document)
        const target = $window.document.getElementById(id)
        console.log(target.offsetTop);
        $window.scrollTo(0, target.offsetTop);
    }

    vm.jumpTo = (id) => {
        $anchorScroll.yOffset = 60;
        $location.hash(id);
        $anchorScroll();
    }
}

controller.$inject = [
    '$state', '$window', '$location', '$anchorScroll'
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

    //require('./basic-info-tile')(module);
};
