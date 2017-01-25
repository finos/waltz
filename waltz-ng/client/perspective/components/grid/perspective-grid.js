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
import {select, selectAll, event} from 'd3-selection';
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
    handlers: '<'
};


const initialState = {
    handlers: {
        onCellClick: d => console.log('perspective-grid:onCellClick', d)
    }
};


const template = require('./perspective-grid.html');


let width = 600;
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


/**
 * { mA -> mB -> { measurableA, measurableB, rating } }
 * @param overrides
 */
function nestOverrides(overrides = []) {
    return nest()
        .key(d => d.measurableA)
        .key(d => d.measurableB)
        .rollup(xs => xs[0])
        .object(overrides);
}


function draw(elem, perspective, handlers) {
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
        .call(drawGrid, perspective, scales, handlers)
        .call(drawRowTitles, perspective.axes.b, scales)
        .call(drawColTitles, perspective.axes.a, scales)
        ;
}


function drawGrid(selection, perspective, scales, handlers) {
    const rowGroups = selection
        .selectAll('.row')
        .data(perspective.axes.b, d => d.measurable.id);

    const newRows = rowGroups
        .enter()
        .append('g')
        .classed('row', true)
        .attr('transform', d => `translate( 0, ${scales.y(d.measurable.id)} )`);

    rowGroups
        .merge(newRows)
        .call(drawCells, perspective, scales, handlers)
        ;


    selection
        .call(drawOverrides, perspective, scales, handlers);
}


const maybeDrag = function(d, handler) {
    const evt = event;
    if (evt.buttons > 0) {
        handler(d);
    }
};


function drawCells(selection, perspective, scales, handlers) {
    const cellGroups = selection
        .selectAll('.cell')
        .data(row => _.map(
            perspective.axes.a,
            col => ({
                id: col.measurable.id + "_" + row.measurable.id,
                col,
                row})),
            d => d.id);


    const newCellGroups = cellGroups
        .enter()
        .append('g')
        .classed('cell', true)
        .attr('transform', d => `translate( ${scales.x(d.col.measurable.id)}, 0 )`)
        .on('mouseover.custom', handlers.custom.onCellOver)
        .on('mouseleave.custom', handlers.custom.onCellLeave)
        .on('mouseover.base', handlers.base.onCellOver)
        .on('mouseleave.base', handlers.base.onCellLeave)
        .on('click.custom', handlers.custom.onCellClick)
        .on('mouseover.drag', d => maybeDrag(d, _.get(handlers, "custom.onCellDrag")))
        .call(drawInherited, scales);

    cellGroups
        .merge(newCellGroups);
}


function drawOverrides(selection, perspective, scales, handlers) {

    const cellPadding = 2;
    const t = cellPadding;
    const l = cellPadding;
    const b = scales.y.bandwidth() - cellPadding;
    const r = scales.x.bandwidth() - cellPadding;
    const w = scales.x.bandwidth() - (2 * cellPadding);
    const h = scales.y.bandwidth() - (2 * cellPadding);

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

    const drawBoxes = selection => {
        return selection
            .append('rect')
            .classed('cell-override', true)
            .attr('x', l)
            .attr('y', t)
            .attr('width', w)
            .attr('height', h)
            .attr('fill', d => {
                return ragColorScale(d.rating).brighter(1.3);
            })
            ;
    };

    const overrides = selection
        .selectAll('.override')
        .data(
            _.values(perspective.ratings),
            d => `${d.measurableA}_${d.measurableB}_${d.rating}` );

    const newOverrides = overrides
        .enter()
        .append('g')
        .classed('override', true)
        .on('mouseover', c => console.log('c', c))
        .on('mouseover.drag', d => maybeDrag(d, handlers))

    overrides
        .exit()
        .remove();

    newOverrides
        .attr('transform', d => `translate(${ scales.x(d.measurableA) },${ scales.y(d.measurableB) })`)
        .call(drawBoxes)
        .filter(d => d.rating === 'X')
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

    const rowTitles = elem
            .selectAll('.row-titles')
            .selectAll('.row-title')
            .data(axis, d => d.measurable.id)
        ;

    const newTitles = rowTitles
        .enter()
        .append('g')
        .classed('row-title', true)
        ;

    newTitles.call(drawRect);
    newTitles.call(drawText);
}


function drawColTitles(elem, axis, scales) {
    const colTitles = elem
        .selectAll('.col-titles')
        .selectAll('.col-title')
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


function findColTitle(svg, measurableId) {
    return svg.selectAll('.col-title')
        .filter(c => c.measurable.id === measurableId)
}


function findRowTitle(svg, measurableId) {
    return svg.selectAll('.row-title')
        .filter(c => c.measurable.id === measurableId)
}


function initSvg(elem, width, height) {
    const svg = select(elem.find('svg')[0]);

    svg.attr('width', width + margin.left + margin.right)
        .attr('height', height + margin.top + margin.bottom);

    return svg
        .selectAll('.main')
        .attr('transform', `translate(${margin.left}, ${margin.top})`);

}


function initHandlers(svg, customHandlers) {
    return {
        base: {
            onCellLeave: function(d) {
                findColTitle(svg, d.col.measurable.id)
                    .attr('text-decoration', 'none');
                findRowTitle(svg, d.row.measurable.id)
                    .attr('text-decoration', 'none');
            },
            onCellOver: function(d) {
                findColTitle(svg, d.col.measurable.id)
                    .attr('text-decoration', 'underline');
                findRowTitle(svg, d.row.measurable.id)
                    .attr('text-decoration', 'underline');
            }
        },
        custom: customHandlers
    };
}


function controller($element) {
    const vm = initialiseData(this, initialState);

    const debouncedDraw = draw; // _.debounce(draw, 100);

    vm.$onChanges = (c) => {
        if (vm.perspective && vm.handlers) {
            width = cellWidth * vm.perspective.axes.a.length;
            height = cellHeight * vm.perspective.axes.b.length;

            const svg = initSvg($element, width, height);
            const handlers = initHandlers(svg, vm.handlers);

            debouncedDraw(svg, vm.perspective, handlers);
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