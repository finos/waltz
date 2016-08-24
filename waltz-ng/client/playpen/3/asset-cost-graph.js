import {initialiseData} from '../../common';
import _ from 'lodash';
import d3 from 'd3';


const template = "<div class='waltz-asset-cost-graph'><svg></svg></div>";


const bindings = {
    apps: '<',   // [ { ... } ... ]
    amounts: '<',   // [ { v1: id, v2: amt }... ]
    onHover: '<',
    onSelect: '<',
    selected: '<', // id
};


const initialState = {
    amounts: [],
    apps: [],
    currency: '€',
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


const numberFormat = d3.format(",d");


function currencyLogFormat(d) {
    var x = Math.log(d) / Math.log(10) + 1e-6;
    return Math.abs(x - Math.floor(x)) < .5
        ? '€ ' + numberFormat(d)
        : "";
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
    svg.attr(mkSvgAttrs());

    const axis = svg
        .append("g")
        .classed("wacg-axis", true)
        .attr("transform", `translate(${dimensions.margin.right - 12},${dimensions.margin.top})`);

    const graph = svg
        .append('g')
        .attr('transform', `translate(${dimensions.margin.right}, ${dimensions.margin.top})`);

    return { graph, axis };
}


function update(graph, axis, amounts = [], selected = null, handlers) {

    const amountsToDisplay = _.chain(amounts)
        .orderBy(getAmount)
        .value();

    const [minAmount, maxAmount] = d3.extent(amountsToDisplay, getAmount);

    const scales = {
        x: d3.scale
            .linear()
            .domain([0, amounts.length])
            .range([0, dimensions.graph.width]),
        y: d3.scale
            .log()
            .domain([minAmount / 1.5, maxAmount * 1.2])
            .range([dimensions.graph.height, 0])
    };

    const yAxis = d3.svg.axis()
        .scale(scales.y)
        .ticks(4)
        .orient("left")
        .tickFormat(currencyLogFormat);

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
        .attr({
            cy: 0, //() => _.random(0, 10) > 5 ? 0 : dimensions.graph.height,
            cx: (d, idx) => scales.x(idx) + _.random(-100, 100),
            r: 0,
            opacity: 0
        })
        .on("mouseover.tweak", function(d) {
            d3.select(this)
                .classed('wacg-hover', true)
                .attr('r', dimensions.circleSize / 2 * 1.33);
        })
        .on("mouseleave.tweak", function(d) {
            d3.select(this)
                .classed('wacg-hover', false)
                .attr('r', dimensions.circleSize / 2);
        })
        .on("mouseover.notify", handlers.onHover)
        .on("click.notify", handlers.onSelect);

    circles
        .exit()
        .transition()
        .duration(animationDuration / 1.5)
        .attr({
            opacity: 0,
            r: 0
        })
        .remove();

    const radius = dimensions.circleSize / 2;

    circles
        .classed('wacg-selected', (d) => getAppId(d) === selected)
        .transition()
        .duration(animationDuration / 2)
        .attr({
            opacity,
            r: d => getAppId(d) === selected ? radius * 1.5 : radius,
            cx: (d, idx) => scales.x(idx),
            cy: d => scales.y(getAmount(d))
        });
}


function controller($element) {

    const vm = initialiseData(this, initialState);
    const svg = d3.select($element.find('svg')[0]);
    const { graph, axis } = prepareGraph(svg);


    vm.$onChanges = (changes) => {
        const handlers = {
            onSelect: vm.onSelect,
            onHover: vm.onHover
        };
        update(graph, axis, vm.amounts, vm.selected, handlers);
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
