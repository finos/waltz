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
import {scaleLinear} from 'd3-scale';
import {select} from 'd3-selection';
import "d3-selection-multi";
import{perhaps} from '../common';

const bindings = {
    values: '<',
    max: '<'
};


function calculateLayoutData(values = [], xScale) {
    let last = xScale(0);
    return _.map(values, v => {
        const width = v
            ? xScale(v)
            : 0;

        const d = {
            x: last,
            width
        };

        last += width;
        return d;
    });
}


function controller($element) {

    const holder = select($element[0])
        .select('.waltz-simple-stack-chart')

    const svg = holder
        .append('svg')
        .attr('width', 0);

    const ambient = svg
        .append('rect')
        .classed('wssc-ambient', true);


    const update = (values = [], max) => {

        const height = 24;
        const width = perhaps(
            () => $element[0].parentElement.clientWidth || 150,
            150);

        svg
            .attr('width', `${width}`)
            .attr('height', `${height}`);

        const xScale = scaleLinear()
            .domain([0, max])
            .range([
                10,
                width - 30
            ]);

        const coords = calculateLayoutData(values, xScale);

        const stacks = svg.selectAll('.wssc-stack')
            .data(values);

        const newStacks = stacks
            .enter()
            .append('rect')
            .classed('wssc-stack', true)
            .attr("class", (d, idx) => `wssc-stack-${idx}`);

        stacks
            .merge(newStacks)
            .attr('y', 3)
            .attr('height', height - 6)
            .attr("x", (d, idx) => coords[idx].x)
            .attr("width", (d, idx) => coords[idx].width);

        ambient
            .attr('width', width)
            .attr('height', height)
            .attr('x', 0)
            .attr('y', 0);
    };


    const vm = this;

    vm.$onChanges = () => {
        if (vm.values) {
            update(vm.values, vm.max);
        }
    };
}


controller.$inject = [
    '$element'
];


const component = {
    bindings,
    controller,
    template: '<div class="waltz-simple-stack-chart"></div>'
};


export default component;