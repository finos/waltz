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
    let latch = false;

    vm.$onInit = () => angular
        .element($window)
        .on("resize", () => resize($element, $window));

    vm.$onDestroy = () => angular
        .element($window)
        .off("resize", () => resize($element, $window));

    vm.$onChanges = () => {
        if (!vm.diagram) return;

        if (latch === true) {
            return;
        }

        latch = vm.diagram !== null && vm.blockProcessor !== null;
        console.log("diagram", { latch, d: vm.diagram, bp: vm.blockProcessor});

        const svg = $element.append(vm.diagram.svg);

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
