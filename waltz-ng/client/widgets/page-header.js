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
        // angular
        //     .element($window)
        //     .on("scroll", _.throttle(scrollListener, 100));
    };

    vm.$onDestroy = () => {
        // angular
        //     .element($window)
        //     .off("scroll", scrollListener);
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

