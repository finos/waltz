/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

