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


function resize(elem, win, widthPercent, heightPercent) {
    const width = (win.innerWidth * ((widthPercent || 70) / 100)) || 1024;
    select(elem[0])
        .select("svg")
        .attr("width", `${width}px`)
        .attr("height", `${width*((heightPercent || 80) / 100)}px`);
}


function controller($element, $window) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => angular
        .element($window)
        .on("resize", () => resize($element, $window, vm.diagram.displayWidthPercent, vm.diagram.displayHeightPercent));

    vm.$onDestroy = () => angular
        .element($window)
        .off("resize", () => resize($element, $window, vm.diagram.displayWidthPercent, vm.diagram.displayHeightPercent));

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

        resize($element, $window, vm.diagram.displayWidthPercent, vm.diagram.displayHeightPercent);

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
