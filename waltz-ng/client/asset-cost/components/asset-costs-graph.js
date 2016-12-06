import {initialiseData,isEmpty} from '../../common';
import {responsivefy} from '../../common/d3-utils'
import _ from 'lodash';
import {scaleLinear, scaleBand} from 'd3-scale';
import {select} from 'd3-selection';
import {extent} from 'd3-array';
import {axisLeft, axisBottom} from 'd3-axis';
import {format} from 'd3-format';
import 'd3-selection-multi';


const template = "<div class='waltz-asset-costs-graph'></div>";


const bindings = {
    costs: '<'   // [ { v1: id, v2: amt }... ]
};


const initialState = {
    costs: []
};


const dimensions = {
    graph: {
        width: 800,
        height: 300,
    },
    margin: {
        top: 0,
        left: 150,
        right: 50,
        bottom: 50
    },
    circleSize: 24
};


const numberFormat = format(",d");

function controller($element) {
    const vm = initialiseData(this, initialState);

    const svg = select($element.find("div")[0]).append('svg')
        .attr('width', dimensions.graph.width)
        .attr('height', dimensions.graph.height)
        .attr('viewbox', `0 0 ${dimensions.graph.width} ${dimensions.graph.height}`);

    responsivefy(svg);

    vm.$onChanges = (changes) => {
        if (isEmpty(vm.costs)) {
            return;
        }

        const totalExtent = extent(vm.costs, c => c.total);

        const xScale = scaleLinear()
            .range([0, dimensions.graph.width - dimensions.margin.left - dimensions.margin.right])
            .domain([0, totalExtent[1]]);

        const yScale = scaleBand()
            .domain(_.map(vm.costs, c => c.entityRef.name))
            .range([0, dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)])
            .padding(0.2);

        const startColor = "#5DADE2",
            endColor = "#21618C",
            colorScale = scaleLinear()
                .domain(totalExtent)
                .range([startColor, endColor]);

        const g = svg.append('g')
            .attr('transform', `translate(${dimensions.margin.left},${dimensions.margin.top})`);

        const bars = g.selectAll('.wacg-bar')
            .data(vm.costs, d => d.entityRef.id)
            .enter()
            .append('g')
            .classed('wacg-bar', true)
            .attr("transform", (d, i) => `translate(0, ${yScale(d.entityRef.name)})`);

        bars.append('rect')
            .attr('x', 0)
            .attr('y', 0)
            .attr('width', d => xScale(d.total))
            .attr('height', yScale.bandwidth())
            .attr('fill', (d, i) => colorScale(d.total));

        bars.append('text')
            .attr("x", 10)
            .attr("y", yScale.bandwidth() / 2 + 3)  // middle of the bar
            .text(d => numberFormat(d.total));

        const xAxis = axisBottom(xScale).ticks(5);
        g.append('g')
            .attr('transform', `translate(0, ${dimensions.graph.height - (dimensions.margin.top + dimensions.margin.bottom)})`)
            .call(xAxis);

        const yAxis = axisLeft(yScale);
        svg.append('g')
            .attr('transform', `translate(${dimensions.margin.left}, ${dimensions.margin.top})`)
            .call(yAxis);
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
