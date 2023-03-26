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
import {initialiseData} from "../../../common";
import {scaleBand, scaleLinear} from "d3-scale";
import {select} from "d3-selection";
import {extent} from "d3-array";
import {axisLeft} from "d3-axis";
import {format} from "d3-format";
import namedSettings from "../../../system/named-settings";
import {currenciesByCode} from "../../../common/currency-utils";
import {truncateMiddle} from "../../../common/string-utils";


const bindings = {
    costs: "<",
    onSelect: "<?"
};


const template = "<div class='waltz-entity-costs-graph'></div>";
const startColor = "#c4eeff";
const endColor = "#55d3ff";
const transitionDuration = 800;


const initialState = {
    costs: [],
    onSelect: d => console.log("Default entity-cost-graph on-select handler", d)
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
                   refsById,
                   onSelect) {

    const yAxis = axisLeft(yScale)
        .tickFormat(d => {
            const name = _.get(refsById, [ d, 'name'], 'Unknown');
            return truncateMiddle(name, 25);
        });

    container
        .transition()
        .duration(transitionDuration)
        .call(yAxis);


    container
        .selectAll(".tick")
        .on("click", d => onSelect(refsById[d]));

    container
        .selectAll('.tick')
        .append('title')
        .text(d => _.get(refsById, [d, 'name'], ''));
}


function draw(chartBody,
              chartAxis,
              costs = [],
              onSelect,
              currencyFormat) {

    const totalExtent = extent(costs, c => c.amount);

    const xScale = scaleLinear()
        .range([0, dimensions.graph.width - dimensions.margin.left - dimensions.margin.right])
        .domain([0, totalExtent[1]]);

    const yScale = scaleBand()
        .domain(_.map(costs, c => c.entityReference.id))
        .range([0, dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)])
        .padding(0.3);

    const colorScale = scaleLinear()
        .domain(totalExtent)
        .range([startColor, endColor]);

    const bars = chartBody
        .attr("transform", `translate(${dimensions.margin.left},${dimensions.margin.top})`)
        .selectAll(".wacg-bar")
        .data(costs, d => d.entityReference.id);

    bars
        .exit()
        .remove();

    const newBars = bars
        .enter()
        .append("g")
        .classed("wacg-bar", true)
        .attr("transform", (d) => `translate(0, ${yScale(d.entityReference.id)})`)
        .on("click.select", d => onSelect(d.entityReference));

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
        .attr("width", d => xScale(d.amount))
        .attr("height", yScale.bandwidth())
        .attr("fill", (d) => colorScale(d.amount));

    allBars
        .select("text")
        .attr("y", yScale.bandwidth() / 2 + 4)  // middle of the bar
        .text(d => currencyFormat(d.amount));

    allBars
        .transition()
        .duration(transitionDuration)
        .attr("transform", (d) => `translate(0, ${yScale(d.entityReference.id)})`);

    const refsById = _
        .chain(costs)
        .map(d => d.entityReference)
        .keyBy(d => d.id)
        .value();

    drawYAxis(
        yScale,
        chartAxis,
        refsById,
        onSelect);
}


function controller($element, $scope, settingsService) {
    const vm = initialiseData(this, initialState);

    const holder = $element.find("div")[0];
    const svg = select(holder)
        .append("svg")
        .attr("id", "waltz-entity-costs-graph")
        .style("min-height", "300px")
        .style("max-height", "500px")
        .style("width", "100%")
        .attr("preserveAspectRatio", "xMinYMin meet");

    const chartBody = svg
        .append("g");

    const chartAxis = svg
        .append("g")
        .attr("transform", `translate(${dimensions.margin.left}, ${dimensions.margin.top})`);

    let currencyFormat = null;

    const refresh = () => {
        if (! currencyFormat) {
            return;
        }

        dimensions.graph.height = 20 + (vm.costs.length * 20);

        svg.attr("viewBox", `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`);

        draw(
            chartBody,
            chartAxis,
            _.orderBy(vm.costs, d => d.amount * -1),
            x => $scope.$applyAsync(() => vm.onSelect(x)),
            currencyFormat);

    };

    vm.$onInit = () => {
        settingsService
            .findOrDefault(namedSettings.defaultCurrency, "EUR")
            .then(code => {
                const currency = currenciesByCode[code];
                currencyFormat = d => `${currency.symbol}${format(",d")(d)}`;
                refresh();
            });
    };

    vm.$onChanges = refresh;

}


controller.$inject = [
    "$element",
    "$scope",
    "SettingsService"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzEntityCostsGraph"
};
