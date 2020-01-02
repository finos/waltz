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
import angular from "angular";
import {select} from "d3-selection";


const bindings = {
    blockProcessor: "<",
    diagram: "<"
};


function resize(elem, win) {

    const width = win.innerWidth * 0.7 || 1024;
    select(elem[0])
        .select("svg")
        .attr("width", `${width}px`)
        .attr("height", `${width*0.8}px`);
}


function controller($element, $window) {
    const vm = this;

    vm.$onInit = () => angular
        .element($window)
        .on("resize", () => resize($element, $window));

    vm.$onDestroy = () => angular
        .element($window)
        .off("resize", () => resize($element, $window));

    vm.$onChanges = () => {
        if (!vm.diagram) return;

        if (_.isNil(vm.blockProcessor)) return;

        const svg = $element
            .empty()
            .append(vm.diagram.svg);

        resize($element, $window);

        const dataProp = `data-${vm.diagram.keyProperty}`;
        const dataBlocks = svg.querySelectorAll(`[${dataProp}]`);

        const blocks = _.map(
            dataBlocks,
            b => ({
                block: b,
                value: b.attributes[dataProp].value
            }));

        _.each(blocks, vm.blockProcessor);
    };

}


controller.$inject = [
    "$element",
    "$window"
];


export default {
    bindings,
    controller
};
