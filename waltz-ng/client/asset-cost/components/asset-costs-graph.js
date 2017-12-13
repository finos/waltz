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
import {initialiseData, isEmpty} from "../../common";
import {responsivefy} from "../../common/d3-utils";
import _ from "lodash";
import {scaleLinear, scaleBand} from "d3-scale";
import {select} from "d3-selection";
import {extent} from "d3-array";
import {axisLeft, axisBottom} from "d3-axis";
import {format} from "d3-format";
import "d3-selection-multi";
import namedSettings from "../../system/named-settings";
import {currenciesByCode} from "../../common/currency-utils";


const template = `<div class='waltz-asset-costs-graph'></div>`;

const bindings = {
    costs: '<',
    onHover: '<',
    onSelect: '<'
};


const initialState = {
    costs: [],
    onHover: _.identity,
    onSelect: _.identity
};


const startColor = "#F6F9EC";
const endColor = "#B3C95A";


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


function processCosts(costs = []) {
    return _.chain(costs)
        .reduce((acc, x) => {
            const bucket = acc[x.application.id] || {total: 0, entityRef: x.application, costs: {}};
            bucket.costs[x.cost.costKind] = x.cost.amount;
            bucket.total += x.cost.amount;

            acc[x.application.id] = bucket;
            return acc;
        }, {})
        .values()
        .orderBy('total', 'desc')
        .value();
}


function drawXAxis(xScale, container, currencyFormat) {
    const xAxis = axisBottom(xScale)
        .tickFormat(currencyFormat)
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


function draw(svg, costs = [],
              onHover = _.identity,
              onSelect = _.identity,
              currencyFormat) {
    const totalExtent = extent(costs, c => c.total);

    const xScale = scaleLinear()
        .range([0, dimensions.graph.width - dimensions.margin.left - dimensions.margin.right])
        .domain([0, totalExtent[1]]);

    const yScale = scaleBand()
        .domain(_.map(costs, c => c.entityRef.name))
        .range([0, dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)])
        .padding(0.2);

    const colorScale = scaleLinear()
        .domain(totalExtent)
        .range([startColor, endColor]);

    const g = svg
        .append('g')
        .attr('transform', `translate(${dimensions.margin.left},${dimensions.margin.top})`);

    const bars = g
        .selectAll('.wacg-bar')
        .data(costs, d => d.entityRef.id)
        .enter()
        .append('g')
        .classed('wacg-bar', true)
        .attr("transform", (d, i) => `translate(0, ${yScale(d.entityRef.name)})`)
        .on("mouseenter.hover", d => onHover(d))
        .on("mouseleave.hover", d => onHover(null))
        .on("click.select", d => onSelect(d));

    bars.append('rect')
        .attr('x', 0)
        .attr('y', 0)
        .attr('width', d => xScale(d.total))
        .attr('height', yScale.bandwidth())
        .attr('fill', (d, i) => colorScale(d.total));

    bars.append('text')
        .attr("x", 10)
        .attr("y", yScale.bandwidth() / 2 + 3)  // middle of the bar
        .text(d => currencyFormat(d.total));

    drawXAxis(xScale, g, currencyFormat);
    drawYAxis(yScale, svg);
}


function controller($element, $scope, settingsService) {
    const vm = initialiseData(this, initialState);

    const holder = $element.find('div')[0];
    const svg = select(holder)
        .append('svg')
        .attr('id', 'waltz-asset-costs-graph');

    let unregisterResponsivefy = () => {};
    let currencyFormat = null;


    const refresh = () => {
        if (isEmpty(vm.costs) || ! currencyFormat) {
            return;
        }

        const aggCosts = processCosts(vm.costs);

        dimensions.graph.height = 100 + (aggCosts.length * 20);

        svg.attr('width', dimensions.graph.width)
            .attr('height', dimensions.graph.height)
            .attr('viewbox', `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`);

        draw(
            svg,
            aggCosts,
            x => $scope.$applyAsync(() => vm.onHover(x)),
            x => $scope.$applyAsync(() => vm.onSelect(x)),
            currencyFormat);

        unregisterResponsivefy();
        $scope.$applyAsync(() => unregisterResponsivefy = responsivefy(svg, 'width-only'));
    };


    vm.$onDestroy = () => unregisterResponsivefy();

    vm.$onInit = () => {
        settingsService
            .findOrDefault(namedSettings.defaultCurrency, 'EUR')
            .then(code => {
                const currency = currenciesByCode[code]
                currencyFormat = d => `${currency.symbol}${format(",d")(d)}`;
                refresh();
            })
    };

    vm.$onChanges = refresh;

}


controller.$inject = [
    '$element',
    '$scope',
    'SettingsService'
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzAssetCostsGraph'
};
