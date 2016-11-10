import d3 from 'd3';
import _ from 'lodash';
import moment from 'moment';

import {initialiseData} from '../../../common';
import {variableScale} from '../../../common/colors';


const bindings = {
    points: '<',
    options: '<'
};


const template = require('./entity-statistic-history-chart.html');


const initialState = {
};


const animationDuration = 200;


function prepareSections(svg) {
    const chart = svg
        .append('g');
    const xAxis = chart
        .append('g')
        .classed('weshc-axis', true);
    const yAxis = chart
        .append('g')
        .classed('weshc-axis', true);
    return {
        svg,
        chart,
        xAxis,
        yAxis
    };
}


function mkDimensions(width = 400) {
    const height = 200;

    const margins = {
        left: 60,
        right: 20,
        top: 20,
        bottom: 30
    };

    const chart = {
        width: width - (margins.left + margins.right),
        height: height - (margins.top + margins.bottom),
    };

    return {
        width,
        height,
        margins,
        chart
    };
}


function mkScales(points = [], dimensions) {
    const countExtent = [0, d3.max(points, d => d.count)];
    const dateExtent = d3.extent(points, d => d.date);
    const xRange = [0, dimensions.width - (dimensions.margins.left + dimensions.margins.right)];
    const yRange = [dimensions.height - (dimensions.margins.top + dimensions.margins.bottom), 0];

    return {
        x: d3.scale
            .linear()
            .domain(dateExtent)
            .range(xRange),
        y: d3.scale
            .linear()
            .domain(countExtent)
            .range(yRange)
    };
}


function drawPoints(section, points = [], scales, options) {
    const pointSelection = section
        .selectAll('.point')
        .data(points, p => p.date + '_' + p.series);

    pointSelection
        .enter()
        .append('circle')
        .classed('point', true)
        .attr({
            fill: d => variableScale(d.series),
            opacity: 0.7,
            r: 0
        })
        .attr();

    pointSelection
        .exit()
        .transition()
        .duration(animationDuration)
        .attr({r: 0})
        .remove();

    pointSelection
        .attr({
            cx: p => scales.x(p.date),
            cy: p => scales.y(p.count)
        })
        .transition()
        .duration(50)
        .attr({
            r: p => {
                return options.highlightedDate && p.date.getTime() === options.highlightedDate.getTime()
                    ? 6
                    : 3
            }
        });
}


function drawLines(section, points = [], scales) {
    const lineFunction = d3.svg
        .line()
        .x(d => scales.x(d.date))
        .y(d => scales.y(d.count))
        .interpolate("linear");

    const bySeries = d3
        .nest()
        .key(d => d.series)
        .entries(points);

    const pathSelector = section
        .selectAll('.line')
        .data(bySeries, s => s.key);

    pathSelector
        .enter()
        .append("path")
        .classed('line', true)
        .attr({
            stroke: d => variableScale(d.key),
            "stroke-width": 1,
            fill: 'none',
            opacity: 0.7
        });

    pathSelector
        .exit()
        .transition()
        .duration(animationDuration)
        .attr({opacity: 0})
        .remove();

    pathSelector
        .transition()
        .duration(animationDuration)
        .attr("d", d => lineFunction(d.values))
}


function adjustSections(sections, dimensions) {
    sections
        .svg
        .attr({
            width: dimensions.width,
            height: dimensions.height
        });

    sections
        .chart
        .attr({
            transform: `translate(${ dimensions.margins.left }, ${ dimensions.margins.top })`
        });

    sections
        .xAxis
        .attr({
            transform: `translate(0, ${ dimensions.height - (dimensions.margins.top + dimensions.margins.bottom) })`
        });
}


function tryInvoke(callback, arg) {
    if (callback) callback(arg);
    else console.log("No callback registed");
}


function drawBands(section,
                   points,
                   scales,
                   options) {

    const bandWidth = 16;
    const extraHeight = 30;

    const dates = _.chain(points)
        .map('date')
        .uniqBy(d => d.getTime())
        .value();

    const bands = section
        .selectAll('rect.band')
        .data(dates, d => d.getTime());

    bands
        .enter()
        .append('rect')
        .classed("band", true)
        .attr({
            'pointer-events': 'all',
        })
        .style('visibility', 'hidden')
        .on('mouseenter.band-hover', d => { tryInvoke(options.onHover, d); })
        .on('mouseleave.band-hover', () => { tryInvoke(options.onHover, null); })
       ;

    bands
        .attr({
            x: d => scales.x(d) - (bandWidth / 2),
            y: extraHeight / 2 * -1,
            width: bandWidth,
            height: scales.y.range()[0] + extraHeight,
        });
}


function pickDatesForXAxis(scale) {
    const dateDomain = scale.domain();
    const start = moment(dateDomain[0]);
    const end = moment(dateDomain[1]);

    const diff = end.diff(start, 'days');
    const jump = Math.ceil(diff / 16);

    if (diff < 2) {
        return _.map(
            _.range(diff + 1),
            x => start.add(x, 'days').toDate().getTime());
    }

    let ptr = start.add(jump, 'days');
    const dates = [];
    while (ptr.isBefore(end)) {
        dates.push(ptr.toDate().getTime());
        ptr = ptr.add(jump * 2, 'days');
    }

    return dates;
}


function drawXAxis(section, points = [], scale) {
    if (points.length == 0) return;

    const dates = pickDatesForXAxis(scale);
    const dateFormat = d3.time.format("%d/%m");

    const xAxis = d3.svg
        .axis()
        .tickValues(dates)
        .tickSize(6)
        .scale(scale)
        .tickFormat(d => { return dateFormat(new Date(d)); })
        .orient("bottom");

    section
        .call(xAxis);
}


function drawYAxis(section, scale) {
    const yAxis = d3.svg
        .axis()
        .ticks(5)
        .scale(scale)
        .orient("left");

    section
        .call(yAxis);
}


function drawAxes(sections, points = [], scales) {
    drawXAxis(sections.xAxis, points, scales.x);
    drawYAxis(sections.yAxis, scales.y);
}


function draw(sections, width, points = [], options = {}) {
    const dimensions = mkDimensions(width);
    const scales = mkScales(points, dimensions);

    adjustSections(sections, dimensions);
    drawAxes(sections, points, scales);
    drawLines(sections.chart, points, scales);
    drawPoints(sections.chart, points, scales, options);
    drawBands(sections.chart, points, scales, options);
}


/**
 * Note: it is v. important the $element is an element with some width,
 * simply placing this in a element like a waltz-section will cause it
 * to render with 0x0....
 * @param $element
 * @param $window
 * @param dataTypeService
 */
function controller($element, $window) {

    const vm = initialiseData(this, initialState);
    const svg = d3.select($element.find('svg')[0]);

    const svgSections = prepareSections(svg);

    const render = () => {
        const elemWidth = $element
            .parent()[0]
            .clientWidth;
        draw(svgSections, elemWidth, vm.points, vm.options);
    };

    const debouncedRender = _.debounce(render, 100);

    vm.$onChanges = (changes) => debouncedRender();

    angular
        .element($window)
        .on('resize', () => debouncedRender());
}


controller.$inject = [
    '$element',
    '$window'
];


const component = {
    bindings,
    template,
    controller
};


export default component;
