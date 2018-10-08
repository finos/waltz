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
import {initialiseData, invokeFunction} from "../../../common";

import template from "./dynamic-section-navigation.html";
import {scaleLinear} from "d3-scale";
import {rgb} from "d3-color";

export const dyamicSectionNavigationDefaultOffset = 250;

const bindings = {
    availableSections: "<",
    openSections: "<",
    parentEntityRef: "<",
    onSelect: "<",
    offset: "@?"
};


const initialState = {
    sections: [],
    offset: dyamicSectionNavigationDefaultOffset,
    stickyVisible: false,
    onSelect: (w) => console.log("default on-select handler for dynamic-section-navigation: ", w),
};


function controller($scope,
                    $window) {
    const vm = initialiseData(this, initialState);

    const colorScale = scaleLinear()
        .range([rgb("#ffc46e"), rgb("#ffffff")]);

    const scrollListener = () => {
        $scope.$applyAsync(() => {
            vm.stickyVisible = $window.pageYOffset > vm.offset;
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

    vm.$onChanges = () => {
        const fadeFactor = 2.5;
        colorScale
            .domain([0, _.get(vm, ["availableSections", "length"],  1) / fadeFactor]);

        vm.sections = _.map(vm.availableSections, s => {
            const openOffset = _.findIndex(vm.openSections, os => os.id === s.id);

            const color = openOffset > -1
                ? rgb(colorScale(openOffset))
                : rgb(255,255,255);

            const style = {
                "border-bottom": `2px solid ${color.toString()}`
            };

            return Object.assign({}, s, { style })
        });
    };

    // -- INTERACT --

    vm.scrollAndSelectSection = (section) => {
        invokeFunction(vm.onSelect, section);
        $window.scrollTo(0, vm.offset);
    };

}


controller.$inject = [
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
