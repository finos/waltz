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

import _ from 'lodash';
import moment from 'moment';
import {extent, max} from 'd3-array';
import {axisBottom, axisLeft} from 'd3-axis';
import {nest} from 'd3-collection';
import {scaleLinear} from 'd3-scale';
import {select} from 'd3-selection';
import {curveLinear, line} from 'd3-shape';
import {timeFormat} from 'd3-time-format';
import 'd3-selection-multi';

import {initialiseData, isEmpty} from '../../../common';
import {variableScale} from '../../../common/colors';
import template from './entity-statistic-history-chart.html';


const bindings = {
    points: '<',
    onHover: '<',
    highlightedDate: '<'
};


const initialState = {
};


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
    const countExtent = [0, max(points, d => d.count)];
    const dateExtent = extent(points, d => d.date);

    const xRange = [0, dimensions.width - (dimensions.margins.left + dimensions.margins.right)];
    const yRange = [dimensions.height - (dimensions.margins.top + dimensions.margins.bottom), 0];

    return {
        x: scaleLinear()
            .domain(dateExtent)
            .range(xRange),
        y: scaleLinear()
            .domain(countExtent)
            .range(yRange)
    };
}


function drawPoints(section, points = [], scales) {
    const pointSelection = section
        .selectAll('.point')
        .data(points, p => p.date + '_' + p.series);

    const newPoints = pointSelection
        .enter()
        .append('circle')
        .classed('point', true)
        .attrs({
            fill: d => variableScale(d.series),
            opacity: 0.7,
            r: 0
        });

    pointSelection
        .exit()
        .remove();

    pointSelection
        .merge(newPoints)
        .attr("cx", p => scales.x(p.date))
        .attr("cy", p => scales.y(p.count))
        .attr('r', 3);

}


function drawLines(section, points = [], scales) {
    const lineFunction = line()
        .x(d => scales.x(d.date))
        .y(d => scales.y(d.count))
        .curve(curveLinear);

    const bySeries = nest()
        .key(d => d.series)
        .entries(points);

    const pathSelector = section
        .selectAll('.line')
        .data(bySeries, s => s.key);

    const newLines = pathSelector
        .enter()
        .append("path")
        .classed('line', true)
        .attrs({
            stroke: d => variableScale(d.key),
            "stroke-width": 1,
            fill: 'none',
            opacity: 0.7
        });

    pathSelector
        .exit()
        .remove();

    pathSelector
        .merge(newLines)
        .attr("d", d => lineFunction(d.values))
}


function adjustSections(sections, dimensions) {
    sections
        .svg
        .attrs({
            width: dimensions.width,
            height: dimensions.height
        });

    sections
        .chart
        .attr('transform', `translate(${ dimensions.margins.left }, ${ dimensions.margins.top })`);

    sections
        .xAxis
        .attr('transform', `translate(0, ${ dimensions.height - (dimensions.margins.top + dimensions.margins.bottom) })`);
}


function tryInvoke(callback, arg) {
    if (callback) callback(arg);
    else console.log("No callback registed");
}


function drawBands(section,
                   points,
                   scales,
                   onHover) {

    const numDates = _.uniqBy(_.map(points, 'date'), d => d + "").length;
    const width = scales.x.range()[1];
    const bandWidth = width / numDates;
    const extraHeight = 30;

    const dates = _.chain(points)
        .map('date')
        .uniqBy(d => d.getTime())
        .value();

    const bands = section
        .selectAll('rect.band')
        .data(dates, d => d.getTime());

    const newBands = bands
        .enter()
        .append('rect')
        .classed("band", true)
        .attr('pointer-events', 'all')
        .style('visibility', 'hidden')
        .on('mouseenter.band-hover', d => {
            tryInvoke(onHover, d);
        })
        .on('mouseleave.band-hover', () => {
            tryInvoke(onHover, null);
        })
       ;

    bands
        .merge(newBands)
        .attrs({
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
    if (isEmpty(points)) return;

    const dates = pickDatesForXAxis(scale);
    const dateFormat = timeFormat("%d/%m");

    const xAxis = axisBottom()
        .tickValues(dates)
        .tickSize(6)
        .scale(scale)
        .tickFormat(d => { return dateFormat(new Date(d)); });

    section
        .call(xAxis);
}


function drawYAxis(section, scale) {
    const yAxis = axisLeft()
        .ticks(5)
        .scale(scale);

    section
        .call(yAxis);
}


function drawAxes(sections, points = [], scales) {
    drawXAxis(sections.xAxis, points, scales.x);
    drawYAxis(sections.yAxis, scales.y);
}


function draw(sections,
              width,
              points = [],
              onHover = () => console.log('weshc: no onHover provided')) {
    const dimensions = mkDimensions(width);
    const scales = mkScales(points, dimensions);

    adjustSections(sections, dimensions);
    drawAxes(sections, points, scales);
    drawLines(sections.chart, points, scales);
    drawPoints(sections.chart, points, scales);
    drawBands(sections.chart, points, scales, onHover);
}


/**
 * Note: it is v. important the $element is an element with some width,
 * simply placing this in a element like a waltz-section will cause it
 * to render with 0x0....
 * @param $element
 * @param $window
 */
function controller($element, $window) {

    const vm = initialiseData(this, initialState);
    const svg = select($element.find('svg')[0]);

    const svgSections = prepareSections(svg);

    const render = () => {
        const elemWidth = $element
            .parent()[0]
            .clientWidth;
        draw(svgSections, elemWidth, vm.points, vm.onHover);
    };

    const debouncedRender = _.debounce(render, 100);

    const isHighlighted = p => vm.highlightedDate && p.date.getTime() === vm.highlightedDate.getTime();

    vm.$onChanges = (changes) => {
        if (changes.highlightedDate) {
            svg.selectAll(".point")
                .attr("r", p => isHighlighted(p) ? 6 : 3);
        } else {
            debouncedRender();
        }
    };

    vm.$onInit = () => angular
        .element($window)
        .on('resize', debouncedRender);

    vm.$onDestroy = () => angular
        .element($window)
        .off('resize', debouncedRender);
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
