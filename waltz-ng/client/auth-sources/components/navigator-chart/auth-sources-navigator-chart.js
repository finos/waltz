

/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import template from './auth-sources-navigator-chart.html';
import {initialiseData} from "../../../common";

import {ragColorScale} from '../../../common/colors';

import _ from 'lodash';
import { select } from 'd3-selection';
import {scaleBand, scaleLinear} from 'd3-scale';
import {truncateText} from "../../../common/d3-utils";


const bindings = {
    navigator: '<'
};


const initialState = {

};


const margin = {top: 100, right: 10, bottom: 20, left: 10};
const totalWidth = 1024;
const width = totalWidth - margin.left - margin.right;

const blocks = 32;
const blockHeight = 20;
const blockWidth = width / blocks;

const blockText = {
    dy: blockHeight * -0.3,
    dx: 3
};
const colWidth = 32;
const rowGroupPadding = 4;
const minHeight = 350;

const blockScaleX = scaleLinear().domain([0,32]).range([0, width]);


const styles = {
    region: 'wasnc-region',
    rowGroupHover: 'wasnc-rowGroupHover',
    colHeader: 'wasnc-colHeader',
    colHover: 'wasnc-colHover',
    appMapping: 'wasnc-appMapping',
    rowGroup: 'wasnc-rowGroup',
    appRow: 'wasnc-appRow',
    history: 'wasnc-history'
};


const regionInfo = [
    {
        name: 'xHeader',
        x: 5,
        y: 0,
        w: blocks - 5
    }, {
        name: 'yHeader',
        x: 1,
        y: 1,
        w: 4
    }, {
        name: 'xHistory',
        x: 10,
        y: 0,
        w: blocks - 5
    }, {
        name: 'yHistory',
        x: 0,
        y: 1,
        w: 1
    }, {
        name: 'rowGroups',
        x: 2,
        y: 1,
        w: blocks - 1
    }
];


// -- UTIL --

function calcTotalRequiredHeight(chartData) {
    const top = blockHeight * 2;
    const groupsHeight = _
        .chain(chartData.rowGroups)
        .map(g => g.rows.length)
        .sum()
        .value() * blockHeight;

    const groupsPadding = chartData.rowGroups.length * rowGroupPadding;

    const total = top + groupsHeight + groupsPadding + margin.top + margin.bottom;
    return _.max([total, minHeight]);
}


function applyBlockTextAttrs(selection) {
    return selection
        .attr('dy', blockText.dy)
        .attr('dx', blockText.dx);
}


/**
 * Each row group may have a variable number of applications (rows)
 * within it.  This function will return a map, keyed by the
 * groups 'domain.id', which gives the 'y' offsets for each group.
 *
 * @param rowGroups
 * @returns { <groupDomainId>: { yOffset, ... }
 */
function calculateRowGroupOffsets(rowGroups = []) {
    return _.reduce(
        rowGroups,
        (acc, g) => {
            const rowCount = g.rows.length;
            const start = acc.total;
            const height = (rowCount * blockHeight);

            acc[g.domain.id] = { start, height };

            acc.total = start + height + rowGroupPadding;
            return acc;
        },
        { total: 0 });
}


const opacityScores = {
    DIRECT: 1,
    HEIR: 0.7,
    ANCESTOR: 0.6,
    UNKNOWN: 0.4
};


const calcOpacity = d => {
    const total = (opacityScores[d.rowType] || 0.5) + (opacityScores[d.colType] || 0.5);
    return total / 2;
};


const highlightColumn = (colId, flag, svg) => {
    svg.selectAll(`.${styles.appMapping}`)
        .filter(d => d.colId === colId)
        .classed(styles.colHover, flag);

    svg.selectAll(`.${styles.colHeader}`)
        .filter(d => d.id === colId)
        .classed(styles.colHover, flag);
};


// -- DRAW FNs --


function init(svg, scaleX) {

    const chart = svg
        .append('g')
        .attr('transform', `translate(${margin.left}, ${margin.top})`);

    chart.selectAll(`.${styles.region}`)
        .data(regionInfo, d => d.name)
        .enter()
        .append('g')
        .classed(styles.region, true)
        .attr('transform', d => `translate(${scaleX(d.x)}, ${blockHeight * d.y})`)
        .each(function (d) {
            select(this).classed(d.name, true)
        });

    return chart;
}


function drawAppMappings(selector, colScale, navigator, svg) {
    const appMappings = selector
        .selectAll(`.${styles.appMapping}`)
        .data(d => d.mappings, d => d.colId);

    appMappings.exit().remove();

    const newAppMappings = appMappings
        .enter()
        .filter(d => d.colType != 'NONE')
        .append('g')
        .classed(styles.appMapping, true)
        .attr('transform', d => `translate(${colScale(d.colId)} , 0)`)
        .on('mouseover', d => highlightColumn(d.colId, true, svg))
        .on('mouseout', d => highlightColumn(d.colId, false, svg))
        .on('click', d => navigator.focusBoth(d.colId, d.groupId));

    newAppMappings
        .append('rect')
        .attr('stroke', d => ragColorScale(d.rating))
        .attr('fill', d => ragColorScale(d.rating).brighter(2))
        .attr('y', 1)
        .attr('x', (colScale.bandwidth() / 2) * -1)
        .attr('width', colScale.bandwidth())
        .attr('height', blockHeight - 2);

    // newAppMappings
    //     .append('text')
    //     .attr('text-anchor', 'middle')
    //     .attr('y', blockHeight)
    //     .attr('dy', blockText.dy)
    //     .style('font-size', 'xx-small');
    //     .text(d => `${d.rowType.charAt(0)}\\${d.colType.charAt(0)}::${d.rating}`)

    return newAppMappings
        .merge(appMappings)
        .style('opacity', calcOpacity);
}


function drawAppRows(selector, colScale, navigator, svg) {
    const appRows = selector
        .selectAll(`.${styles.appRow}`)
        .data(d => d.rows, d => d.app.id);

    const newAppRows = appRows
        .enter()
        .append('g')
        .classed(styles.appRow, true);

    newAppRows
        .merge(appRows)
        .attr('transform', (d, i) => `translate(${blockWidth * 3}, ${i * blockHeight})`);

    newAppRows
        .append('text')
        .text(d => d.app.name)
        .attr('y', blockHeight)
        .call(applyBlockTextAttrs)
        .call(truncateText, blockWidth * 4);

    appRows.exit().remove();

    appRows
        .merge(newAppRows)
        .call(drawAppMappings, colScale, navigator, svg);

    return appRows;
}


function drawRowGroupLabel(selection) {
    return selection
        .append('text')
        .attr('y', blockHeight)
        .text(d => d.domain.name)
        .call(applyBlockTextAttrs)
        .call(truncateText, blockWidth * 3 - 10)
}


function drawRowGroups(navigator, svg, chartData, colScale) {

    const groupOffsets = calculateRowGroupOffsets(chartData.rowGroups);

    const rowGroups = svg
        .select('.rowGroups')
        .selectAll(`.${styles.rowGroup}`)
        .data(chartData.rowGroups, d => d.domain.id);

    rowGroups.exit().remove();

    const newRowGroups = rowGroups
        .enter()
        .append('g')
        .classed(styles.rowGroup, true);

    newRowGroups
        .append('rect')
        .attr('width', 900)
        .attr('height', d => groupOffsets[d.domain.id].height)
        .attr('fill', (d,i) => i % 2 ? '#fafafa': '#f3f3f3');

    newRowGroups
        .merge(rowGroups)
        .attr('transform', d => `translate(0, ${groupOffsets[d.domain.id].start})`);

    newRowGroups
        .on('mouseover.hover', function() { select(this).classed(styles.rowGroupHover, true)})
        .on('mouseout.hover', function() { select(this).classed(styles.rowGroupHover, false)})
        .on('click.focus', d => navigator.focusRowGroup(d.domain.id))
        .call(drawRowGroupLabel);

    rowGroups
        .merge(newRowGroups)
        .call(drawAppRows, colScale, navigator, svg);
}


function drawColHeaders(navigator, svg, chartData, colScale) {
    const cols = chartData.cols;

    const headers = svg
        .select(".xHeader")
        .selectAll(`.${styles.colHeader}`)
        .data(cols.domain, d => d.id);

    headers.exit().remove();

    const newHeaders = headers
        .enter()
        .append('g')
        .classed(styles.colHeader, true)
        .attr('transform', d => `translate(${colScale(d.id)}, 0) rotate(315 0,14)`)
        .on('mouseover', d => highlightColumn(d.id, true, svg))
        .on('mouseout', d => highlightColumn(d.id, false, svg))
        .on('click', d => navigator.focusCol(d.id));

    newHeaders
        .append('text')
        .text(d => d.name)
        .attr('y', 16)
        .call(truncateText, blockWidth * 4)

    return newHeaders.merge(headers);
}


function drawHistory(navigator, svg, chartData) {
    const xHistory = svg
        .select('.xHistory')
        .selectAll('text')
        .data(chartData.cols.active
            ? [chartData.cols.active]
            : [],
            d => d.id);

    xHistory.exit().remove();

    xHistory
        .enter()
        .append('text')
        .text(d => `.. up to ${d.name}`)
        .classed(styles.history, true)
        .classed('clickable', true)
        .attr('transform', `rotate(315 0,${blockHeight}) `)
        .on('click', d => navigator.focusCol(d.parentId))
        .call(truncateText, blockWidth * 4);

    const yHistory = svg
        .select('.yHistory')
        .selectAll('text')
        .data(
            chartData.rows.active
                ? [chartData.rows.active]
                : [],
            d => d.id);

    yHistory.exit().remove();

    yHistory
        .enter()
        .append('text')
        .text(d => `.. up to ${d.name}`)
        .classed('clickable', true)
        .classed(styles.history, true)
        .attr('text-anchor', 'end')
        .attr('y', blockWidth)
        .attr('dy', blockHeight)
        .attr('transform', `rotate(270 0,${blockHeight}) `)
        .on('click', d => navigator.focusRowGroup(d.parentId));
}

function draw(navigator, chartData, svg, blockScaleX) {
    console.log('draw', chartData);
    global.chart = chartData;

    if (! svg) return;

    const height = calcTotalRequiredHeight(chartData);

    svg.attr('viewBox', `0 0 1024 ${height}`);

    const colDomain = chartData.cols.domain || [];
    const colsWidth = colWidth * colDomain.length;
    const colsStartX = blockScaleX(5);

    const colScale = scaleBand()
        .domain(_.map(colDomain, 'id'))
        .range([colsStartX, colsStartX + colsWidth ])
        .paddingInner([0.1])
        .paddingOuter([0.3])
        .align([0.5]);

    drawColHeaders(navigator, svg, chartData, colScale);
    drawRowGroups(navigator, svg, chartData, colScale);
    drawHistory(navigator, svg, chartData);

}


// --- NG ---

function controller($element) {
    const vm = initialiseData(this, initialState);

    let svg = null;

    vm.$onInit = () => {
        const rootElem = $element[0];
        svg = select(rootElem)
            .select('svg');

        init(svg, blockScaleX);
    };

    vm.$onChanges = (c) => {
        if (c.navigator && vm.navigator) {
            vm.navigator.addListener((chart) => draw(vm.navigator, chart, svg, blockScaleX));
        }
    };
}


controller.$inject = ['$element'];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzAuthSourcesNavigatorChart',
    component
}