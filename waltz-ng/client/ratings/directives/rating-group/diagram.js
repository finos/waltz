

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {select} from "d3-selection";
import 'd3-selection-multi';

import {RIGHT_ARROW, DOWN_ARROW} from "../../../common/arrows";
import {defaultDimensions, setupSummaryBarScales, setupCellScale} from "../common.js";
import {capabilityColorScale} from "../../../common/colors";
import RatingRows from "./rating-rows";


const GroupSummaryCellBars = {
    className: 'group-summary-cell-bar',
    enter: (selection) => selection
        .append('rect')
        .classed(GroupSummaryCellBars.className, true)
        .attr('width', 0),

    update: (selection, barScale) => selection
        .attrs({
            fill: ({rating}) => capabilityColorScale(rating).brighter(),
            stroke: ({rating}) => capabilityColorScale(rating),
            transform: ({rating}) => `translate(0, ${barScale.y(rating)})`,
            height: barScale.y.rangeBand()
        })
        .transition()
        .duration(150)
        .attr('width', ({count}) => barScale.x(count))
};


const GroupSummaryCells = {
    className: 'group-summary-cell',

    prepareData: (cs) => _.chain(cs)
        .omit('measurable')
        .map((v, k) => ({rating: k, count: v}))
        .value(),

    enter: (selection) => selection
        .append('g')
        .classed(GroupSummaryCells.className, true),


    update: (selection, cellsScale, barScale) => {

        selection
            .attr('transform', d => `translate(${cellsScale(d.measurable)}, 0)`);

        const charts = selection
            .selectAll('.' + GroupSummaryCellBars.className)
            .data(GroupSummaryCells.prepareData);

        charts.enter()
            .call(GroupSummaryCellBars.enter);

        charts
            .call(GroupSummaryCellBars.update, barScale);

        charts
            .exit()
            .remove();

    }
};


function renderSummaries(container, data, dimensions, scales) {

    const rowHeight = (dimensions.ratingCell.height + dimensions.ratingCell.padding);

    const rowLabels = container
        .selectAll('.group-name')
        .data([data.groupRef], d => d.id);

    rowLabels
        .enter()
        .append('text')
        .classed('group-name', true)
        .classed('no-text-select', true)
        .attrs({
            'transform': `translate(${dimensions.label.width}, ${ rowHeight / 1.6})`,
            'text-anchor': 'end',
            'font-size': 'larger',
            'font-weight': 'bolder'
        });

    rowLabels
        .text((d) => {
            const arrow = data.collapsed ? RIGHT_ARROW : DOWN_ARROW;
            return arrow + ' ' + _.truncate(d.name, { length: 20 });
        });

    const groupSummaryCells = container
        .selectAll('.' + GroupSummaryCells.className)
        .data(data.collapsed ? data.summaries : []);

    groupSummaryCells
        .enter()
        .call(GroupSummaryCells.enter);

    groupSummaryCells
        .call(GroupSummaryCells.update, scales.cells, scales.bar);

    groupSummaryCells
        .exit()
        .remove();
}


function renderRatingsRows(container, data, dimensions, scales, tweakers) {

    const ratingsRows = new RatingRows(dimensions, scales.cells, tweakers);

    ratingsRows.apply(
        container,
        () => data.collapsed ? [] : data.raw,
        rs => rs.subject.id);
}


function setupScales(dimensions, groupData, highestRatingCount) {
    const scales = {};

    scales.cells = setupCellScale(dimensions.label.width, dimensions.viz.width, groupData.measurables);
    scales.bar = setupSummaryBarScales(dimensions, scales.cells, highestRatingCount);

    return scales;
}


function calculateSvgDimensions(width, data) {
    const rowHeight = defaultDimensions.ratingCell.height + defaultDimensions.ratingCell.padding;
    const height
        = defaultDimensions.margin.top
        + defaultDimensions.margin.bottom
        + rowHeight
        + (data.collapsed ? 0 : data.raw.length * rowHeight);

    return { width, height };
}


export function draw(data, width, svg, tweakers, highestRatingCount) {

    const dimensions = { ...defaultDimensions, viz: calculateSvgDimensions(width, data)};

    data.redraw = () => draw(data, width, svg, tweakers, highestRatingCount);

    const scales = setupScales(dimensions, data, highestRatingCount);

    svg.attrs({width: dimensions.viz.width, height: dimensions.viz.height});

    const summaryGroup = svg.select('.summary')
        .attr('transform', `translate(${dimensions.margin.left}, ${dimensions.margin.top})`)
        .on('click', () => { data.collapsed = !data.collapsed; data.redraw(); });

    const rawGroup = svg.select('.raw')
        .attr('transform', `translate(${dimensions.margin.left}, ${dimensions.margin.top})`);

    renderSummaries(summaryGroup, data, dimensions, scales);
    renderRatingsRows(rawGroup, data, dimensions, scales, tweakers);
}


export function init(vizElem) {
    const svg = select(vizElem)
        .append('svg');

    const grid = svg.append('g').classed('grid', true);
    grid.append('g').classed('summary', true);
    grid.append('g').classed('raw', true);

    return svg;
}

