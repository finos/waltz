/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import {initialiseData, invokeFunction} from "../../../common";

import template from "./dynamic-section-navigation.html";
import {scaleLinear} from "d3-scale";
import {rgb} from "d3-color";

export const dynamicSectionNavigationDefaultOffset = 250;

const bindings = {
    availableSections: "<",
    openSections: "<",
    parentEntityRef: "<",
    onSelect: "<",
    offset: "@?"
};


const initialState = {
    offset: dynamicSectionNavigationDefaultOffset,
    stickyVisible: false,
};

const fadeFactor = 2.5;
const colorScale = scaleLinear()
    .range([rgb("#ffc46e"), rgb("#ffffff")]); // orange -> white


function controller(dynamicSectionManager,
                    $scope,
                    $window) {
    const vm = initialiseData(this, initialState);

    const scrollListener = () => {
        $scope.$applyAsync(() => {
            vm.stickyVisible = $window.pageYOffset > vm.offset;
        });
    };

    vm.$onInit = () => {
        angular
            .element($window)
            .on("scroll", _.throttle(scrollListener, 100));

        vm.sections = dynamicSectionManager.getAvailable();

        const numSections = _.get(vm, ["sections", "length"],  1);
        colorScale
            .domain([0, numSections / fadeFactor]);

        vm.activeSections = dynamicSectionManager.getActive();

    };

    vm.$onDestroy = () => {
        angular
            .element($window)
            .off("scroll", scrollListener);
    };

    vm.mkStyle = (section) => {
        const sectionOffset = vm.activeSections.indexOf(section);
        const color = sectionOffset > -1
            ? rgb(colorScale(sectionOffset))
            : rgb(255,255,255);

        return {
            'border-bottom' : `2px solid ${color.toString()}`
        };
    };

    // -- INTERACT --

    vm.onSelect = (section) => {
        dynamicSectionManager.activate(section);
        $window.scrollTo(0, vm.offset);
    };

}


controller.$inject = [
    "DynamicSectionManager",
    "$scope",
    "$window"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzDynamicSectionNavigation"
};
