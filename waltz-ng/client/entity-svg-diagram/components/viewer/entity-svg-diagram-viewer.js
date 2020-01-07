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

import {initialiseData} from "../../../common";
import {kindToViewState} from "../../../common/link-utils";
import {stringToRef} from "../../../common/entity-utils";
import {select, event} from "d3-selection";
import {zoom} from "d3-zoom";

import template from "./entity-svg-diagram-viewer.html";


const bindings = {
    svg: "<"
};


const initialState = {
    panZoomEnabled: false,
    popup: {
        style: {
            position: "absolute",
            background: "white",
            display: "none"
        },
        text: ""
    }
};

const updateTransform = (g, currentZoom, currentTranslate) =>
    g.attr("transform", `scale(${currentZoom}) translate(${currentTranslate})`);


function controller($element, $state, $timeout) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const holder = select($element[0])
            .select(".wesd-svg")
            .html(vm.svg);

        let currentZoom = 1;
        let currentTranslate = [0,0];

        const zoomed = () => {
            const transform = event.transform;

            if (transform) {
                currentZoom = transform.k;
                currentTranslate[0] = transform.x;
                currentTranslate[1] = transform.y;
                updateTransform(g, currentZoom, currentTranslate);
            }
        };

        const svg = select($element[0])
            .select("svg");

        const g = svg
            .select("g");

        const myZoom = zoom()
            .on("zoom", zoomed);

        const setupZoom = () => {
            svg.call(myZoom)
                .on("dblclick.zoom", null);
        };

        const teardownZoom = () => {
            svg.on(".zoom", null);
        };

        g.selectAll("[data-wesd-node-entity-link]")
            .on("click", function() {
                const refStr = select(this)
                    .attr("data-wesd-node-entity-link");
                const ref = stringToRef(refStr);
                const viewState = kindToViewState(ref.kind);
                $state.go(viewState, {id: ref.id});
            });

        g.selectAll("[data-wesd-node-description]")
            .on("mouseenter", function() {
                const text = select(this)
                    .attr("data-wesd-node-description");

                let top = 0;
                let left = 0;

                select(this)
                    .each((d,i,g) => {
                        const gBounds = g[0].getBoundingClientRect();
                        const holderBounds = holder.node().getBoundingClientRect();
                        top = gBounds.top - holderBounds.top;
                        left = gBounds.left - holderBounds.left + (gBounds.left > 700 ? -250 : 100);
                    });

                $timeout(() => {
                    vm.popup.style.display = "inline-block";
                    vm.popup.style.left = `${left}px`;
                    vm.popup.text = text;
                });
            })
            .on("mouseleave", function() {
                select(this)
                    .attr("data-wesd-node-description");
                $timeout(() => {
                    vm.popup.style.display = "none";
                    vm.popup.text = "";
                });
            });

        vm.zoom = (delta) => {
            currentZoom += delta;
            updateTransform(g, currentZoom, currentTranslate);
        };

        vm.panX = (delta) => {
            currentTranslate[0] += delta;
            updateTransform(g, currentZoom, currentTranslate);
        };

        vm.panY = (delta) => {
            currentTranslate[1] += delta;
            updateTransform(g, currentZoom, currentTranslate);
        };

        vm.togglePanZoom = () => {
            vm.panZoomEnabled = ! vm.panZoomEnabled;
            if (vm.panZoomEnabled) {
                setupZoom();
            } else {
                teardownZoom();
            }
        };
    };

}


controller.$inject = [
    "$element",
    "$state",
    "$timeout"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEntitySvgDiagramViewer"
};
