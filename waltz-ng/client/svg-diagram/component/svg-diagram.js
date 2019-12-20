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

import template from "./svg-diagram.html";
import _ from "lodash";
import angular from "angular";
import {event, select} from "d3-selection";
import {zoom, zoomIdentity} from "d3-zoom";
import {initialiseData} from "../../common";


const bindings = {
    blockProcessor: "<",
    diagram: "<"
};


const initialState = {
    zoomEnabled: false
};


function resize(elem, win) {

    const width = win.innerWidth * 0.7 || 1024;
    select(elem[0])
        .select("svg")
        .attr("width", `${width}px`)
        .attr("height", `${width*0.8}px`);
}


function controller($element, $window) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => angular
        .element($window)
        .on("resize", () => resize($element, $window));

    vm.$onDestroy = () => angular
        .element($window)
        .off("resize", () => resize($element, $window));

    vm.$onChanges = () => {
        if (!vm.diagram) return;

        if (_.isNil(vm.blockProcessor)) return;

        // remove any existing svg elements
        $element.find("svg").remove();

        // append new svg
        const svgEl = $element
            .append(vm.diagram.svg);

        vm.svgGroupSel = select($element[0])
                            .select("svg")
                            .select("svg > g");

        resize($element, $window);

        const dataProp = `data-${vm.diagram.keyProperty}`;
        const dataBlocks = svgEl.querySelectorAll(`[${dataProp}]`);

        const blocks = _.map(
            dataBlocks,
            b => ({
                block: b,
                value: b.attributes[dataProp].value
            }));

        _.each(blocks, vm.blockProcessor);
    };

    // pan + zoom
    function zoomed() {
        const t = event.transform;

        vm.svgGroupSel
            .attr("transform", t);
    }

    const myZoom = zoom()
        .on("zoom", zoomed);

    vm.enableZoom = () => {
        select($element[0])
            .select("svg")
            .call(myZoom)
            .on("dblclick.zoom", null);

        vm.zoomEnabled = true;
    };

    vm.disableZoom = () => {
        select($element[0])
            .select("svg")
            .on(".zoom", null);

        vm.zoomEnabled = false;
    };

    vm.resetZoom = () => {
        vm.svgGroupSel
            .attr("transform", "scale(1) translate(0,0)");

        // reset stored transform values
        select($element[0])
            .select("svg")
            .call(zoom().transform, zoomIdentity);
    };
}


controller.$inject = [
    "$element",
    "$window"
];


export default {
    template,
    bindings,
    controller
};
