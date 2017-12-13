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
import {initialiseData, isEmpty} from "../../../common";
import {responsivefy} from "../../../common/d3-utils";
import _ from "lodash";
import {scaleLinear, scaleBand} from "d3-scale";
import {select} from "d3-selection";
import {extent} from "d3-array";
import {axisLeft, axisBottom} from "d3-axis";
import {format} from "d3-format";
import "d3-selection-multi";


const template = `<div class='waltz-complexity-bar-chart'></div>`;

const bindings = {
    complexity: '<',
    apps: '<',
    onSelect: '<'
};


const initialState = {
    complexity: [],
    apps: [],
    onHover: _.identity,
    onSelect: _.identity
};


const numberFormat = format(".2f");
const complexityFormat = d => `${numberFormat(d)}`;

const startColor = "#FFF3E5";
const endColor = "#FFA54F";


const dimensions = {
    graph: {
        width: 600
    },
    margin: {
        top: 0,
        left: 150,
        right: 50,
        bottom: 50
    },
    circleSize: 24
};


function prepareChartData(complexity = [], apps = []) {
    const appsByIds = _.keyBy(apps, 'id');

    return _.chain(complexity)
        .orderBy('overallScore', 'desc')
        .take(10)
        .map(c => Object.assign(c, { app: appsByIds[c.id] }))
        .value();
}


function drawXAxis(xScale, container) {
    const xAxis = axisBottom(xScale)
        .tickFormat(complexityFormat)
        .ticks(5);

    container.append('g')
        .attr('transform', `translate(0, ${dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)})`)
        .call(xAxis);
}


function drawYAxis(yScale, container) {
    const yAxis = axisLeft(yScale);

    container.append('g')
        .attr('transform', `translate(${dimensions.margin.left}, ${dimensions.margin.top})`)
        .call(yAxis);
}


function draw(svg,
              complexityChartData = [],
              onHover = _.identity,
              onSelect = _.identity) {

    const overallScoreExtent = extent(complexityChartData, c => c.overallScore);

    const xScale = scaleLinear()
        .range([0, dimensions.graph.width - dimensions.margin.left - dimensions.margin.right])
        .domain([0, overallScoreExtent[1]]);

    const yScale = scaleBand()
        .domain(_.map(complexityChartData, c => c.app.name))
        .range([0, dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)])
        .padding(0.2);

    const colorScale = scaleLinear()
        .domain(overallScoreExtent)
        .range([startColor, endColor]);

    const g = svg
        .append('g')
        .attr('transform', `translate(${dimensions.margin.left},${dimensions.margin.top})`);

    const bars = g
        .selectAll('.wcbc-bar')
        .data(complexityChartData, d => d.id)
        .enter()
        .append('g')
        .classed('wcbc-bar', true)
        .attr("transform", (d, i) => `translate(0, ${yScale(d.app.name)})`)
        .on("mouseenter.hover", d => onHover(d))
        .on("mouseleave.hover", d => onHover(null))
        .on("click.select", d => onSelect(d));

    bars.append('rect')
        .attr('x', 0)
        .attr('y', 0)
        .attr('width', d => xScale(d.overallScore))
        .attr('height', yScale.bandwidth())
        .attr('fill', (d, i) => colorScale(d.overallScore));

    bars.append('text')
        .attr("x", 10)
        .attr("y", yScale.bandwidth() / 2 + 3)  // middle of the bar
        .text(d => complexityFormat(d.overallScore));

    drawXAxis(xScale, g);
    drawYAxis(yScale, svg);
}


function controller($element, $scope) {
    const vm = initialiseData(this, initialState);

    const holder = $element.find('div')[0];
    const svg = select(holder)
        .append('svg')
        .attr('id', 'waltz-complexity-bar-chart');

    let unregisterResponsivefy = () => {};

    vm.$onDestroy = () => unregisterResponsivefy();

    vm.$onChanges = () => {
        if (isEmpty(vm.complexity) || isEmpty(vm.apps)) {
            return;
        }

        const complexityChartData = prepareChartData(vm.complexity, vm.apps);

        dimensions.graph.height = 100 + (complexityChartData.length * 20);

        svg.attr('width', dimensions.graph.width)
            .attr('height', dimensions.graph.height)
            .attr('viewbox', `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`);

        draw(
            svg,
            complexityChartData,
            x => $scope.$applyAsync(() => vm.onHover(x)),
            x => $scope.$applyAsync(() => vm.onSelect(x)));

        unregisterResponsivefy();
        unregisterResponsivefy = responsivefy(svg, 'width-only');
    };
}


controller.$inject = [
    '$element',
    '$scope'
];


export const component = {
    bindings,
    template,
    controller
};


export const id = "waltzComplexityBarChart";
