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
import _ from "lodash";
import "d3-selection-multi";
import {initialiseData, isEmpty} from "../../../common";
import {scaleBand, scaleLinear} from "d3-scale";
import {select} from "d3-selection";
import {extent} from "d3-array";
import {axisLeft} from "d3-axis";
import {truncateMiddle} from "../../../common/string-utils";


const bindings = {
    complexities: "<",
    onSelect: "<?"
};


const template = "<div class='waltz-complexity-graph'></div>";
const startColor = "#e7e3f9";
const endColor = "#bfb2ff";
const transitionDuration = 800;


const initialState = {
    costs: [],
    onSelect: d => console.log("Default entity-complexity-graph on-select handler", d)
};


const dimensions = {
    graph: {
        width: 600
    },
    margin: {
        top: 0,
        left: 150,
        right: 50,
        bottom: 10
    }
};


function drawYAxis(yScale,
                   container,
                   refsById) {

    const yAxis = axisLeft(yScale)
        .tickFormat(d => truncateMiddle(
            _.get(refsById, [ d, 'name'], 'Unknown'),
            25));

    container
        .transition()
        .duration(transitionDuration)
        .call(yAxis);

    container
        .selectAll('.tick')
        .append('title')
        .text(d => _.get(refsById, [d, 'name'], ''));
}


function draw(chartBody,
              chartAxis,
              complexitites = [],
              onSelect) {

    const totalExtent = extent(complexitites, c => c.score);

    const xScale = scaleLinear()
        .range([0, dimensions.graph.width - dimensions.margin.left - dimensions.margin.right])
        .domain([0, totalExtent[1]]);

    const yScale = scaleBand()
        .domain(_.map(complexitites, c => c.entityReference.id))
        .range([0, dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)])
        .padding(0.3);

    const colorScale = scaleLinear()
        .domain(totalExtent)
        .range([startColor, endColor]);

    const bars = chartBody
        .attr("transform", `translate(${dimensions.margin.left},${dimensions.margin.top})`)
        .selectAll(".wcg-bar")
        .data(complexitites, d => d.entityReference.id);

    bars
        .exit()
        .remove();

    const newBars = bars
        .enter()
        .append("g")
        .classed("wcg-bar", true)
        .attr("transform", (d) => `translate(0, ${yScale(d.entityReference.id)})`)
        .on("click.select", d => onSelect(d));

    newBars
        .append("rect")
        .attr("x", 0)
        .attr("y", 0);

    newBars
        .append("text")
        .attr("x", 10);

    const allBars = bars
        .merge(newBars);

    allBars
        .select("rect")
        .transition()
        .duration(transitionDuration)
        .attr("width", d => xScale(d.score))
        .attr("height", yScale.bandwidth())
        .attr("fill", (d) => colorScale(d.score));

    allBars
        .select("text")
        .attr("y", yScale.bandwidth() / 2 + 4)  // middle of the bar
        .text(d => d.score);

    allBars
        .transition()
        .duration(transitionDuration)
        .attr("transform", (d) => `translate(0, ${yScale(d.entityReference.id)})`);

    const refsById = _
        .chain(complexitites)
        .map(d => d.entityReference)
        .keyBy(d => d.id)
        .value();

    drawYAxis(
        yScale,
        chartAxis,
        refsById,
        onSelect);
}


function controller($element, $scope) {
    const vm = initialiseData(this, initialState);

    const holder = $element.find("div")[0];
    const svg = select(holder)
        .append("svg")
        .attr("id", "waltz-complexity-graph")
        .style("min-height", "300px")
        .style("max-height", "500px")
        .attr("preserveAspectRatio", "xMinYMin meet");

    const chartBody = svg
        .append("g");

    const chartAxis = svg
        .append("g")
        .attr("transform", `translate(${dimensions.margin.left}, ${dimensions.margin.top})`);

    const refresh = () => {
        if (isEmpty(vm.complexities)) {
            return;
        }

        dimensions.graph.height = 20 + (vm.complexities.length * 20);

        svg.attr("viewBox", `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`);

        draw(
            chartBody,
            chartAxis,
            _.orderBy(vm.complexities, d => d.score * -1),
            x => $scope.$applyAsync(() => vm.onSelect(x)));

    };

    vm.$onInit = () => {
        refresh();
    };

    vm.$onChanges = refresh;

}


controller.$inject = [
    "$element",
    "$scope",
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzComplexityGraph"
};
