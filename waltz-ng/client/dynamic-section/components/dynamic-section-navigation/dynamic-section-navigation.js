/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import _ from "lodash";
import {initialiseData} from "../../../common";

import template from "./dynamic-section-navigation.html";
import {scaleLinear} from "d3-scale";
import {rgb} from "d3-color";
import {dynamicSections} from "../../dynamic-section-definitions";


export const dynamicSectionNavigationDefaultOffset = 250;

const bindings = {
    availableSections: "<",
    openSections: "<",
    parentEntityRef: "<",
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

    const scrollListener = _.throttle(
        () => {
            $scope.$applyAsync(() => {
                vm.stickyVisible = $window.pageYOffset > vm.offset;
            });
        },
        150);

    function enableScrollListener() {
        angular
            .element($window)
            .on("scroll", scrollListener);
    }

    vm.$onInit = () => {
        enableScrollListener();

        vm.sections = dynamicSectionManager.getAvailable();

        vm.sectionsById = _.keyBy(dynamicSections, d => d.id);

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
            "border-bottom" : `2px solid ${color.toString()}`
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
