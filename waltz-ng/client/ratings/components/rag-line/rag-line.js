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

import _ from "lodash";
import {select} from "d3-selection";
import {scaleLinear} from "d3-scale";
import "d3-selection-multi";

const bindings = {
    scores: "<",
    range: "<",
    ratingSchemeItems: "<"
};


const template = "<svg class=\"rag-line\"></svg>";

function controller($element) {

    const width = 250;
    const height = 3;

    const vm = this;

    const svg = select($element[0])
        .select("svg")
        .attrs({ width: `${width}px`, height: `${height}px` });

    const update = (scores, range = [0, 0], ratingSchemeItems) => {
        const xScale = scaleLinear()
            .domain([0, range[1]])
            .range([0, width]);

        let ptr = 0;
        const data = _
            .chain(ratingSchemeItems)
            .filter(r => scores[r.rating] > 0)
            .map(r => {
                const width = scores[r.rating];
                const result = Object.assign({}, r, { start: ptr, width });
                ptr = ptr + width;
                return result;
            })
            .value();

        const rects = svg
            .selectAll("rect")
            .data(data, d => d.rating);

        const newRects = rects
            .enter()
            .append("rect")
            .attr("fill", d => d.color)
            .attr("height", height)
            .attr("y", 0);

        newRects.merge(rects)
            .attr("x", d => xScale(d.start))
            .attr("width", d => xScale(d.width));
    };


    vm.$onChanges = () => {
        if (vm.scores && vm.range && vm.ratingSchemeItems) {
            update(vm.scores, vm.range, vm.ratingSchemeItems);
        }
    };

}


controller.$inject = [
    "$element"
];

const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzRagLine",
    component
};
