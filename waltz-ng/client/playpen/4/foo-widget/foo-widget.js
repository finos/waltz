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
import _ from 'lodash';
import {select, selectAll, event} from 'd3-selection';
import {stack} from 'd3-shape';
import {scaleLinear, scaleBand} from 'd3-scale';

import template from './foo-widget.html';
import {initialiseData} from "../../../common/index";
import {responsivefy} from "../../../common/d3-utils";


// -- d3

const Styles = {
    STACK: 'wfw-stack',
    LAYER: 'wfw-layer',
    LAYER_CELL: 'wfw-layer-cell'
};


const BUCKET_INCREMENTS = 5;



function prepareSvg(holder, dimensions) {
    return select(holder)
        .append('svg')
        .attr("width", dimensions.w)
        .attr("height", dimensions.h)
        .attr('viewBox', `0 0 ${dimensions.w} ${dimensions.h}`);
}


function prepareScales(data = [], dimensions, keys = []) {

    const maxTotal = _
        .chain(data)
        .flatMap(d => _.sum(_.map(keys, k => d[k] || 0)))
        .max()
        .value();


    const yScale = scaleLinear()
        .domain([0, maxTotal])
        .range([dimensions.h, 0]);

    const xScale = scaleBand()
        .domain(_.range(0, Math.ceil(data.length / BUCKET_INCREMENTS) * BUCKET_INCREMENTS))
        .range([0, dimensions.w])
        .padding(0.2);

    return {
        x: xScale,
        y: yScale
    };
}



// -- angular

const initialState = {

};


const bindings = {
    data: '<',
    keys: '<'
};


function enrichDataWithLevels(data = []) {
    return _.map(
        data || [],
        (d, idx) => Object.assign({}, d, {level: idx}));
}


function calcDimensions(holder) {
    return {
        w: holder.clientWidth,
        h: holder.clientHeight
    };
}


function draw(svg, stackData, keys = [], scales) {
    const layers = svg
        .selectAll(`.${Styles.LAYER}`)
        .data(stackData);

    const newLayers = layers
        .enter()
        .append('g')
        .classed(Styles.LAYER, true)
        .attr('data-key', (d, idx) => keys[idx]);

    layers
        .merge(newLayers)
        .call(drawCells, scales);
}


function drawCells(layers, scales) {
    const cells = layers
        .selectAll(`.${Styles.LAYER_CELL}`)
        .data((d) => d);

    const newCells = cells
        .enter()
        .append('rect')
        .classed(Styles.LAYER_CELL, true);

    newCells
        .merge(cells)
        .attr('width', scales.x.bandwidth)
        .attr('x', d => scales.x(d.data.level))
        .attr('y', d => scales.y(d[1]))
        .attr('height', d => scales.y(d[0]) - scales.y(d[1]));
}


function controller($element) {
    const vm = initialiseData(this, initialState);

    function refresh() {
        const data = enrichDataWithLevels(vm.data);
        const stackData = stack()
            .keys(vm.keys)
            (data);
        const scales = prepareScales(data, vm.dimensions, vm.keys);
        draw(vm.svg, stackData, vm.keys, scales);
    }

    vm.$onInit = () => {
        const holder = $element.find('div')[0];
        vm.dimensions = calcDimensions(holder);
        vm.svg = prepareSvg(holder, vm.dimensions);
        refresh();
    };

    vm.$onChanges = () => {
        if (vm.svg) {
            refresh();
        }

    }
}


controller.$inject = [
    '$element'
];


export const component = {
    bindings,
    controller,
    template
};


export const id = 'waltzFooWidget';