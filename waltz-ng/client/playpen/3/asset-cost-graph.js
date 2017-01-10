/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {initialiseData} from '../../common';
import _ from 'lodash';
import {scaleLinear, scaleLog} from 'd3-scale';
import {select} from 'd3-selection';
import {extent} from 'd3-array';
import {axisLeft} from 'd3-axis';
import {format} from 'd3-format';
import 'd3-selection-multi';


const template = "<div class='waltz-asset-cost-graph'><svg></svg></div>";


const bindings = {
    apps: '<',   // [ { ... } ... ]
    amounts: '<',   // [ { v1: id, v2: amt }... ]
    onHover: '<',
    onSelect: '<',
    selected: '<', // id
    scaleType: '<'
};


const initialState = {
    amounts: [],
    apps: [],
    currency: '€',
    scaleType: 'linear',
    onHover: (d) => console.log('asset-cost-graph: default on-hover', d),
    onSelect: (d) => console.log('asset-cost-graph: default on-select', d)
};


const animationDuration = 1000;


const dimensions = {
    graph: {
        width: 800,
        height: 200,
    },
    margin: {
        top: 20,
        left: 20,
        right: 100,
        bottom: 50
    },
    circleSize: 24
};


const numberFormat = format(",d");


function currencyLogFormat(d) {
    const x = Math.log(d) / Math.log(10) + 1e-6;
    return Math.abs(x - Math.floor(x)) < .5
        ? '€ ' + numberFormat(d)
        : "";
}


function currencyFormat(d) {
    return '€ ' + numberFormat(d);
}


function calculateOpacity(size = 1) {
    return _.max([
        10 / Math.sqrt(size * 40),
        0.1
    ]);
}


function getAppId(a) {
    return a.v1;
}


function getAmount(a) {
    return a.v2;
}


function mkSvgAttrs() {
    const { graph, margin } = dimensions;
    return {
        width: graph.width + margin.left + margin.right,
        height: graph.height + margin.top + margin.bottom
    };
}


function prepareGraph(svg) {
    svg.attrs(mkSvgAttrs());

    const axis = svg
        .append("g")
        .classed("wacg-axis", true)
        .attr("transform", `translate(${dimensions.margin.right - 12},${dimensions.margin.top})`);

    const graph = svg
        .append('g')
        .attr('transform', `translate(${dimensions.margin.right}, ${dimensions.margin.top})`);

    return { graph, axis };
}


function mkScales(amounts = [], scaleType = 'log') {
    const [minAmount, maxAmount] = extent(amounts, getAmount);

    const baseYScale = scaleType === 'log'
            ? scaleLog()
            : scaleLinear();

    return {
        x: scaleLinear()
            .domain([0, amounts.length])
            .range([0, dimensions.graph.width]),
        y: baseYScale
            .domain([minAmount / 1.5, maxAmount * 1.2])
            .range([dimensions.graph.height, 0])
    }
}


function mkAxis(scale, scaleType = 'log') {
    const axis = axisLeft()
        .scale(scale);

    if (scaleType === 'log') {
        axis.ticks(5)
            .tickFormat(currencyLogFormat);
    }

    if (scaleType === 'linear') {
        axis.ticks(5)
            .tickFormat(currencyFormat);
    }
    return axis;
}


function update(
    { graph, axis },  // unpack
    amounts = [],
    selected = null,
    handlers) {

    const amountsToDisplay = _.chain(amounts)
        .orderBy(getAmount)
        .value();

    const scales = mkScales(amountsToDisplay, "linear");

    const yAxis = mkAxis(scales.y, "linear");

    axis.call(yAxis);

    // hand-wavy opacity algorithm goes here
    const opacity = calculateOpacity(amountsToDisplay.length);

    const circles = graph
        .selectAll('.wacg-amount')
        .data(amountsToDisplay, getAppId);

    circles
        .enter()
        .append('circle')
        .classed('wacg-amount', true)
        .attrs({
            cy: 0, //() => _.random(0, 10) > 5 ? 0 : dimensions.graph.height,
            cx: (d, idx) => scales.x(idx) + _.random(-100, 100),
            r: 0,
            opacity: 0
        })
        .on("mouseover.tweak", function(d) {
            select(this)
                .classed('wacg-hover', true)
                .attr('r', dimensions.circleSize / 2 * 1.33);
        })
        .on("mouseleave.tweak", function(d) {
            select(this)
                .classed('wacg-hover', false)
                .attr('r', dimensions.circleSize / 2);
        })
        .on("mouseover.notify", handlers.onHover)
        .on("click.notify", handlers.onSelect);

    circles
        .exit()
        .transition()
        .duration(animationDuration / 1.5)
        .attrs({
            opacity: 0,
            r: 0
        })
        .remove();

    const radius = dimensions.circleSize / 2;

    circles
        .classed('wacg-selected', (d) => getAppId(d) === selected)
        .transition()
        .duration(animationDuration / 2)
        .attrs({
            opacity,
            r: d => getAppId(d) === selected ? radius * 1.5 : radius,
            cx: (d, idx) => scales.x(idx),
            cy: d => scales.y(getAmount(d))
        });
}


function controller($element) {

    const vm = initialiseData(this, initialState);
    const svg = select($element.find('svg')[0]);
    const svgSections = prepareGraph(svg);


    vm.$onChanges = (changes) => {
        const handlers = {
            onSelect: vm.onSelect,
            onHover: vm.onHover
        };
        update(svgSections, vm.amounts, vm.selected, handlers);
    };

}


controller.$inject = [
    '$element'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
