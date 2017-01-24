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

import _ from 'lodash';
import {scaleBand} from 'd3-scale';
import {select, selectAll} from 'd3-selection';
import {path} from 'd3-path';
import {nest} from 'd3-collection';
import {initialiseData} from '../../../common';
import {ragColorScale} from '../../../common/colors';


/**
 * @name waltz-perspective-grid
 *
 * @description
 * This component ...
 */


const bindings = {
    perspective: '<',
    onCellClick: '<'
};


const initialState = {
    onCellClick: d => console.log('perspective-grid:onCellClick', d)
};


const template = require('./perspective-grid.html');


let width = 1000;
let height = 300;

const margin = {
    top: 100, bottom: 50,
    left: 200, right: 50
};

const labelIndicatorSize = 10;
const labelIndicatorPadding = 4;
const labelPadding = labelIndicatorSize + labelIndicatorPadding * 2;
const cellWidth = 40;
const cellHeight = 35;

function nestOverrides(overrides = []) {
    return nest()
        .key(d => d.measurableA)
        .key(d => d.measurableB)
        .rollup(xs => xs[0])
        .object(overrides);
}


function draw(elem, perspective, onCellClick) {
    if (! perspective) return;

    const scales = {
        x: scaleBand()
            .domain(_.map(perspective.axes.a, 'measurable.id'))
            .range([0, width]),
        y: scaleBand()
            .domain(_.map(perspective.axes.b, 'measurable.id'))
            .range([0, height])
    };

    elem
        .call(drawGrid, perspective, scales, onCellClick)
        .call(drawRowTitles, perspective.axes.b, scales)
        .call(drawColTitles, perspective.axes.a, scales)
        ;
}


function drawGrid(selection, perspective, scales, onCellClick) {
    const rowGroups = selection
        .selectAll('.row')
        .data(perspective.axes.b, d => d.measurable.id);

    rowGroups
        .enter()
        .append('g')
        .classed('row', true)
        .attr('transform', d => `translate( 0, ${scales.y(d.measurable.id)} )`)
        .call(drawCells, perspective, scales, onCellClick)
        ;
}


function drawCells(selection, perspective, scales, onCellClick) {
    const cellGroups = selection
        .selectAll('.cell')
        .data(row => _.map(
            perspective.axes.a,
            col => ({
                id: col.measurable.id + "_" + row.measurable.id,
                col,
                row})),
            d => d.id);

    cellGroups
        .enter()
        .append('g')
        .classed('cell', true)
        .attr('transform', d => `translate( ${scales.x(d.col.measurable.id)}, 0 )`)
        .on('mouseover.cell', onCellClick)
        .call(drawInherited, scales)
        .call(drawOverrides, perspective, scales);
}


function drawOverrides(selection, perspective, scales) {
    const overrides = nestOverrides(perspective.ratings);

    const cellPadding = 2;
    const t = cellPadding;
    const l = cellPadding;
    const b = scales.y.bandwidth() - cellPadding;
    const r = scales.x.bandwidth() - cellPadding;
    const w = scales.x.bandwidth() - (2 * cellPadding);
    const h = scales.y.bandwidth() - (2 * cellPadding);

    const getOverride = (idA, idB) => {
        const overridesForA = overrides[idA] || {};
        return overridesForA[idB];
    };

    const getOverrideRating = (idA, idB) => {
        return (getOverride(idA, idB) || {}).rating;
    };

    const drawXShape = (selection) => {
        const d = path();
        d.moveTo(l, t);
        d.lineTo(r, b);
        d.moveTo(l, b);
        d.lineTo(r, t);

        selection
            .append('path')
            .attr('d', d)
            .attr('stroke', '#ddd');
    };

    const boxes = selection
        .filter(d => getOverride(d.col.measurable.id, d.row.measurable.id))
        .append('rect')
        .classed('cell-override', true)
        .attr('x', l)
        .attr('y', t)
        .attr('width', w)
        .attr('height', h)
        .attr('fill', d => {
            const override = getOverride(d.col.measurable.id, d.row.measurable.id);
            return ragColorScale(override.rating).brighter(1.3);
        })
        ;

    boxes
        .filter(d => getOverrideRating(d.col.measurable.id, d.row.measurable.id) !== 'X')
        .attr('stroke', '#aaa')
        .attr('stroke-width', 1.5)
        ;

    selection
        .filter(d => getOverrideRating(d.col.measurable.id, d.row.measurable.id) === 'X')
        .call(drawXShape);

}


function drawInherited(selection, scales) {
    const cellPadding = 6;

    const t = cellPadding;
    const l = cellPadding;
    const b = scales.y.bandwidth() - cellPadding;
    const r = scales.x.bandwidth() - cellPadding;
    const w = scales.x.bandwidth() - (2 * cellPadding);
    const h = scales.y.bandwidth() - (2 * cellPadding);

    const tl = `${l} ${t}`;
    const bl = `${l} ${b}`;
    const tr = `${r} ${t}`;
    const br = `${r} ${b}`;

    const drawColTriangles = (selection) => selection
        .append('polyline')
        .classed('cell-inherited', true)
        .attr('points', `${tl} ${tr} ${br}`)
        .attr('stroke', '#ccc')
        .attr('fill', d => ragColorScale(d.col.rating.rating).brighter(2))
        ;

    const drawRowTriangles = (selection) => selection
        .append('polyline')
        .classed('cell-inherited', true)
        .attr('points', `${tl} ${bl} ${br}`)
        .attr('stroke', '#ccc')
        .attr('fill', d => ragColorScale(d.row.rating.rating).brighter(2))
        ;

    selection
        .filter(d => d.row.rating.rating !== d.col.rating.rating)
        .call(drawColTriangles)
        .call(drawRowTriangles)
        ;

    selection
        .filter(d => d.row.rating.rating === d.col.rating.rating)
        .append('rect')
        .classed('cell-inherited', true)
        .attr('stroke', '#ccc')
        .attr('fill', d => ragColorScale(d.row.rating.rating).brighter(2))
        .attr('x', l)
        .attr('y', t)
        .attr('width', w)
        .attr('height', h)
        ;
}


function drawRowTitles(elem, axis, scales) {
    const rowTitles = elem
        .append('g')
        .classed('row-titles', true)
        .selectAll('row-title')
        .data(axis, d => d.measurable.id)
        ;

    const drawRect = (selection) => selection
        .append('rect')
        .attr('x', (labelIndicatorPadding + labelIndicatorSize) * -1)
        .attr('y', d => scales.y(d.measurable.id) + labelIndicatorPadding)
        .attr('width', labelIndicatorSize)
        .attr('height', scales.y.bandwidth() - labelIndicatorPadding * 2)
        .attr('fill', d => ragColorScale(d.rating.rating).brighter(1.5))
        .attr('stroke', d => ragColorScale(d.rating.rating))
        ;

    const drawText = (selection) => selection
        .append('text')
        .classed('row-title', true)
        .text(d => d.measurable.name)
        .attr('text-anchor', 'end')
        .attr('transform', d => `translate(-${labelPadding},  ${scales.y(d.measurable.id) + scales.y.bandwidth() / 2})`)
        ;

    rowTitles
        .enter()
        .call(drawRect)
        .call(drawText)
        ;
}


function drawColTitles(elem, axis, scales) {
    const colTitles = elem
        .append('g')
        .classed('col-titles', true)
        .selectAll('col-title')
        .data(axis, d => d.measurable.id)
        ;

    const drawRect = (selection) => selection
        .append('rect')
        .attr('x', d => scales.x(d.measurable.id) + labelIndicatorPadding)
        .attr('y', (labelIndicatorPadding + labelIndicatorSize) * -1)
        .attr('width', scales.x.bandwidth() - labelIndicatorPadding * 2)
        .attr('height', labelIndicatorSize)
        .attr('fill', d => ragColorScale(d.rating.rating).brighter(1.5))
        .attr('stroke', d => ragColorScale(d.rating.rating))
        ;

    const drawText = selection => selection
        .append('text')
        .classed('col-title', true)
        .text(d => d.measurable.name)
        .attr('text-anchor', 'start')
        .attr('transform', d =>
            `translate(${scales.x(d.measurable.id) + scales.x.bandwidth() / 3}, -${labelPadding}) rotate(-20)`)
        ;

    colTitles
        .enter()
        .call(drawRect)
        .call(drawText)
        ;
}


function initSvg(elem, width, height) {
    const svg = select(elem.find('svg')[0]);

    return svg
        .attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom)
        .append('g')
        .attr('transform', `translate(${margin.left}, ${margin.top})`);
}


function controller($element) {
    const vm = initialiseData(this, initialState);


    vm.$onChanges = (c) => {
        if (c.perspective) {
            console.log(vm.perspective)
            if (vm.perspective) {
                width = cellWidth * vm.perspective.axes.a.length;
                height = cellHeight * vm.perspective.axes.b.length;

                const svg = initSvg($element, width, height);

                draw(svg, vm.perspective, vm.onCellClick);
            }
        }
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


export default component;