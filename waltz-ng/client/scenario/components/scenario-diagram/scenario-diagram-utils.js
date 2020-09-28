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
import {zoom, zoomIdentity} from "d3-zoom";
import {event} from "d3-selection";


export const defaultOptions = { cols: 3, sortFn: d => d.node.name };


const myZoom = zoom()
    .scaleExtent([0.1, 2]);


export function setupZoom(svgGroups) {
    const zoomer = myZoom.on("zoom.myZoom", () => {
        const tx = event.transform.x;
        const ty = event.transform.y;
        const k = event.transform.k;

        svgGroups
            .rowAxisContent
            .attr("transform", `translate(0 ${ty}) scale(${k})`);

        svgGroups
            .columnAxisContent
            .attr("transform", `translate(${tx} 0) scale(${k})`);

        svgGroups
            .gridContent
            .attr("transform", event.transform);
    });

    return svgGroups
        .svg
        .call(zoomer);
}


export function removeZoom(svgGroups) {
    return svgGroups
        .svg
        .on(".zoom", null);
}


export function resetZoom(svgGroups) {
    svgGroups
        .svg
        .transition()
        .duration(800)
        .call(myZoom.transform, zoomIdentity.scale(1).translate(1, 1));
}

