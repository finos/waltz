import {initialiseData,isEmpty} from '../../common';
import {responsivefy} from '../../common/d3-utils'
import _ from 'lodash';
import {scaleLinear, scaleBand} from 'd3-scale';
import {select} from 'd3-selection';
import {extent} from 'd3-array';
import {axisLeft, axisBottom} from 'd3-axis';
import {format} from 'd3-format';
import 'd3-selection-multi';


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


const numberFormat = format(",d");
const currencyFormat = d => `€${numberFormat(d)}`;

const startColor = "#5DADE2";
const endColor = "#21618C";


const dimensions = {
    graph: {
        width: 600,
        height: 250,
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
            bucket.costs[x.cost.kind] = x.cost.amount;
            bucket.total += x.cost.amount;

            acc[x.application.id] = bucket;
            return acc;
        }, {})
        .values()
        .orderBy('total', 'desc')
        .value();
}


function drawXAxis(xScale, container) {
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
              onSelect = _.identity) {
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

    drawXAxis(xScale, g);
    drawYAxis(yScale, svg);
}


function controller($element, $scope) {
    const vm = initialiseData(this, initialState);

    const holder = $element.find('div')[0];
    const svg = select(holder)
        .append('svg')
        .attr('id', 'waltz-asset-costs-graph')
        .attr('width', dimensions.graph.width)
        .attr('height', dimensions.graph.height)
        .attr('viewbox', `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`);

    let unregisterResponsivefy = () => {};

    vm.$onDestroy = () => unregisterResponsivefy();

    vm.$onInit = () => {
        $scope.$applyAsync(() => {
            unregisterResponsivefy = responsivefy(svg)
        });
    };

    vm.$onChanges = () => {
        if (isEmpty(vm.costs)) {
            return;
        }

        const aggCosts = processCosts(vm.costs);
        draw(
            svg,
            aggCosts,
            x => $scope.$applyAsync(() => vm.onHover(x)),
            x => $scope.$applyAsync(() => vm.onSelect(x)));
    };
}


controller.$inject = [
    '$element',
    '$scope'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
