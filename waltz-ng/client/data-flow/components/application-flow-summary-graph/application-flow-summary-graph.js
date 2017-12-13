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
import {select} from "d3-selection";
import {scaleLinear} from "d3-scale";

import {initialiseData} from '../../../common';
import {authoritativeRatingBackgroundColorScale, authoritativeRatingColorScale} from "../../../common/colors";

import template from './application-flow-summary-graph.html';


const bindings = {
    summaryData: '<'
};


const initialState = {

};


const rowHeight = 30;
const rowPadding = 5;
const h = rowHeight * 3;
const colWidth = 120;
const colPadding = 5;
const w = colWidth * 3;
const keys = ['NO_OPINION', 'DISCOURAGED', 'SECONDARY', 'PRIMARY'];


const cellTransforms = {
    INBOUND: {
        KNOWN: `translate(0, ${rowHeight}) rotate(180, ${colWidth / 2}, ${rowHeight / 2}) `,
        UNKNOWN: `translate(0, ${rowHeight * 2}) rotate(180, ${colWidth / 2}, ${rowHeight / 2})`
    },
    OUTBOUND: {
        KNOWN: `translate(${colWidth * 2}, ${rowHeight})`,
        UNKNOWN: `translate(${colWidth * 2}, ${rowHeight * 2})`
    }
};


const styles = {
    BAND: 'wafsg-band',
    BAR: 'wafsg-bar'
};


function drawBackground(svg) {
    const verticalLines = [colWidth, colWidth * 2];

    svg.selectAll('line')
        .data(verticalLines)
        .enter()
        .append('line')
        .attr('x1', d => d)
        .attr('x2', d => d)
        .attr('y1', rowHeight)
        .attr('y2', h)
        .attr('stroke', '#ddd')
}


function drawCenterLabels(svg) {
    const labels = svg
        .append('g')
        .style('border', '1px solid red')
        .attr('transform', `translate(${colWidth}, ${rowHeight})`);

    const labelData = [
        'Known',
        'Unknown'];

    labels
        .selectAll('text')
        .data(labelData)
        .enter()
        .append('text')
        .text(d => d)
        .attr('text-anchor', 'middle')
        .attr('x', colWidth / 2)
        .attr('y', (d, idx) => rowHeight * (idx + 0.5) + 4);
}


function drawTitleBar(svg) {
    const titleBar = svg
        .append('g')
        .attr('transform', `translate(0, ${rowHeight / 2})`);

    const titleData = [
        { title: 'Inbound', padding: -4, x: colWidth, anchor: 'end' },
        { title: 'Outbound', padding: 4, x: colWidth * 2, anchor: 'start' }];

    titleBar
        .selectAll('text')
        .data(titleData)
        .enter()
        .append('text')
        .text(d => d.title)
        .attr('text-anchor', d => d.anchor)
        .attr('x', d => d.x + d.padding);
}


function enrichData(data = []) {
    return _.chain(data)
        .flatMap((vs, k) => {
            return _.map(vs, (authCounts, rating) => {
                return {
                    row: k,
                    col: rating,
                    values: authCounts
                };
            })
        })
        .map(enrichCellData)
        .value(data);
}


function enrichCellData(data = []) {
    let ptr = 0;
    const stack = [];
    _.forEach(keys, k => {
        const v = data.values[k] || 0;
        const nextPtr = ptr + v;
        stack.push([ptr, nextPtr]);
        ptr = nextPtr;
    });
    return Object.assign({}, data, { stack, total: ptr });
}


function drawData(svg, data = []) {

    if (! svg) return;

    const maxSize = _
        .chain(data)
        .map(d => d.total)
        .max()
        .value();

    const actualColWidth = colWidth - (colPadding * 2);

    const scale = scaleLinear()
        .domain([0, maxSize])
        .range([0, actualColWidth]);

    const bars = svg.selectAll(`.${styles.BAR}`)
        .data(data);

    const newBars = bars
        .enter()
        .append('g')
        .classed(styles.BAR, true)
        .attr('transform', d => cellTransforms[d.row][d.col]);


    const bands = newBars
        .selectAll(`.${styles.BAND}`)
        .data(d => d.stack);

    bands.enter()
        .append('rect')
        .classed(styles.BAND, true)
        .attr('x', d => scale(d[0]))
        .attr('y', rowPadding)
        .attr('height', rowHeight - (2 * rowPadding))
        .attr('width', d => scale(d[1] - d[0]))
        .attr('stroke', (d, idx) => authoritativeRatingBackgroundColorScale(keys[idx]))
        .attr('stroke-width', 0.5)
        .attr('fill', (d, idx) => authoritativeRatingColorScale(keys[idx]));
}



function controller($element) {
    const vm = initialiseData(this, initialState);

    let svg = null;
    const redraw = () => drawData(svg, enrichData(vm.summaryData));

    vm.$onChanges = () => redraw();

    vm.$onInit = () => {
        const holder = $element[0].firstChild;

        svg = select(holder)
            .attr('width', w)
            .attr('height', h);

        drawBackground(svg);
        drawTitleBar(svg);
        drawCenterLabels(svg);
        redraw();
    };
}


controller.$inject = [
    '$element'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzApplicationFlowSummaryGraph'
};
