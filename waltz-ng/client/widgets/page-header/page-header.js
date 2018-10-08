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
import _ from "lodash";
import angular from "angular";
import {initialiseData} from "../../common";

import template from "./page-header.html";

export const pageHeaderDefaultOffset = 60;

const bindings = {
    name: "@",
    icon: "@",
    small: "@",
    tour: "<"
};


const transclude = true;


const initialState = {
    stickyVisible: false
};


function controller($document,
                    $scope,
                    $window) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        $document[0].title = `Waltz: ${vm.name}`;
    };

    const scrollListener = () => {
        $scope.$applyAsync(() => {
            vm.stickyVisible = $window.pageYOffset > pageHeaderDefaultOffset
        });
    };

    vm.$onInit = () => {
        angular
            .element($window)
            .on("scroll", _.throttle(scrollListener, 100));
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

    vm.scrollToTop = () => {
        $window.scrollTo(0, 0);
    };

}


controller.$inject=[
    "$document",
    "$scope",
    "$window"
];


const component = {
    bindings,
    template,
    controller,
    transclude
};

export default component;

