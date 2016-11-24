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

import angular from 'angular';
import {initialiseData} from '../common';

const bindings = {
    name: '@',
    icon: '@',
    small: '@',
    tour: '<'
};


const transclude = true;


const initialState = {

};


function controller($document,
                    $scope,
                    $window) {
    const vm = initialiseData(this, initialState);

    vm.sticky = false;

    vm.$onChanges = () => {
        $document[0].title = `Waltz: ${vm.name}`;
    };

    const scrollListener = () => {
        $scope.$apply(() => {
            vm.sticky = $window.pageYOffset > 60
        });
    };

    vm.$onInit = () => {
        angular
            .element($window)
            .on("scroll", scrollListener);
    };

    vm.$onDestroy = () => {
        angular
            .element($window)
            .off("scroll", scrollListener);
    };

    vm.startTour = () => {
        if (vm.tour) {
            vm.tour.start();
        }
    };

}


controller.$inject=[
    '$document',
    '$scope',
    '$window'
];


const template = require('./page-header.html');


const component = {
    bindings,
    template,
    controller,
    transclude
};

export default component;

