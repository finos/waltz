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
import {initialiseData, isEmpty} from "../../../common";
import _ from "lodash";
import {scaleLinear, scaleBand} from "d3-scale";
import {select} from "d3-selection";
import {extent} from "d3-array";
import {axisLeft, axisBottom} from "d3-axis";
import {format} from "d3-format";
import "d3-selection-multi";


const template = "<div class='waltz-complexity-bar-chart'></div>";

const bindings = {
    complexity: "<",
    apps: "<",
    onSelect: "<"
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
    const appsByIds = _.keyBy(apps, "id");

    return _.chain(complexity)
        .orderBy("overallScore", "desc")
        .take(10)
        .map(c => Object.assign(c, { app: appsByIds[c.id] }))
        .value();
}


function drawXAxis(xScale, container) {
    const xAxis = axisBottom(xScale)
        .tickFormat(complexityFormat)
        .ticks(5);

    container.append("g")
        .attr("transform", `translate(0, ${dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)})`)
        .call(xAxis);
}


function drawYAxis(yScale, container) {
    const yAxis = axisLeft(yScale);

    container.append("g")
        .attr("transform", `translate(${dimensions.margin.left}, ${dimensions.margin.top})`)
        .call(yAxis);
}


function draw(svg,
              complexityChartData = [],
              onHover = _.identity,
              onSelect = _.identity) {
    // remove any previous elements
    svg.selectAll("*").remove();

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
        .append("g")
        .attr("transform", `translate(${dimensions.margin.left},${dimensions.margin.top})`);

    const bars = g
        .selectAll(".wcbc-bar")
        .data(complexityChartData, d => d.id)
        .enter()
        .append("g")
        .classed("wcbc-bar", true)
        .attr("transform", (d) => `translate(0, ${yScale(d.app.name)})`)
        .on("mouseenter.hover", d => onHover(d))
        .on("mouseleave.hover", () => onHover(null))
        .on("click.select", d => onSelect(d));

    bars.append("rect")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", d => xScale(d.overallScore))
        .attr("height", yScale.bandwidth())
        .attr("fill", (d) => colorScale(d.overallScore));

    bars.append("text")
        .attr("x", 10)
        .attr("y", yScale.bandwidth() / 2 + 3)  // middle of the bar
        .text(d => complexityFormat(d.overallScore));

    drawXAxis(xScale, g);
    drawYAxis(yScale, svg);
}


function calcGraphHeight(complexityChartData) {
    return 100 + (complexityChartData.length * 20);
}


function controller($element, $scope) {
    const vm = initialiseData(this, initialState);

    const holder = $element.find("div")[0];

    vm.$onChanges = () => {
        if (isEmpty(vm.complexity) || isEmpty(vm.apps)) {
            return;
        }

        const complexityChartData = prepareChartData(vm.complexity, vm.apps);

        dimensions.graph.height = calcGraphHeight(complexityChartData);
        const svg = select(holder)
            .append("svg")
            .attr("id", "waltz-complexity-bar-chart")
            .style("min-height", "300px")
            .attr("viewBox", `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`)
            .attr("preserveAspectRatio", "xMinYMin meet")

        draw(
            svg,
            complexityChartData,
            x => $scope.$applyAsync(() => vm.onHover(x)),
            x => $scope.$applyAsync(() => vm.onSelect(x)));
    };
}


controller.$inject = [
    "$element",
    "$scope"
];


export const component = {
    bindings,
    template,
    controller
};


export const id = "waltzComplexityBarChart";
