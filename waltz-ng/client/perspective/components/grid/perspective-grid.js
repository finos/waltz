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

import _ from "lodash";
import {scaleBand} from "d3-scale";
import {event, select} from "d3-selection";
import {path} from "d3-path";
import {initialiseData} from "../../../common";
import {truncateMiddle} from "../../../common/string-utils";
import {mkPerspective} from "../../perpective-utilities";
import template from "./perspective-grid.html";
import {CORE_API} from "../../../common/services/core-api-utils";

/**
 * @name waltz-perspective-grid
 *
 * @description
 * This component ...
 */


const bindings = {
    perspectiveDefinition: '<',
    measurables: '<',
    measurableRatings: '<',
    existingOverrides: '<',
    pendingOverrides: '<',
    handlers: '<',
    hideImplied: '<'
};


const initialState = {
    hideInherited: true,
    handlers: {
        onCellClick: d => console.log('perspective-grid:onCellClick', d)
    }
};




let width = 600;
let height = 300;

const scales = {

};

const margin = {
    top: 100, bottom: 50,
    left: 200, right: 50
};


const labelIndicatorSize = 10;
const labelIndicatorPadding = 4;
const labelPadding = labelIndicatorSize + labelIndicatorPadding * 2;
const cellWidth = 40;
const cellHeight = 35;
const maxLabelLength = 32;


function setupScales(perspective) {
    scales.x = scaleBand()
        .domain(_.map(perspective.axes.x, 'measurable.id'))
        .range([0, width]);
    scales.y = scaleBand()
        .domain(_.map(perspective.axes.y, 'measurable.id'))
        .range([0, height]);
}


function draw(elem, perspective, handlers) {
    if (! perspective) return;

    elem
        .call(drawGrid, perspective, handlers)
        .call(drawColTitles, perspective)
        .call(drawRowTitles, perspective)
        ;
}


function drawGrid(selection, perspective, handlers) {
    const rowGroups = selection
        .selectAll('.row')
        .data(perspective.axes.y, d => d.measurable.id);

    const newRows = rowGroups
        .enter()
        .append('g')
        .classed('row', true)
        .attr('transform', d => `translate( 0, ${scales.y(d.measurable.id)} )`);

    rowGroups
        .merge(newRows)
        .call(drawCells, perspective, handlers)
        ;
}


const maybeDrag = function(d, handler = () => {}) {
    if (event.buttons > 0) {
        handler(d);
    }
};


function drawCells(selection, perspective, handlers) {
    const cellGroups = selection
        .selectAll('.cell')
        .data(row => _.map(
            perspective.axes.x,
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
        .call(drawInherited, perspective);

    cellGroups
        .merge(newCellGroups);
}


function drawXShape(selection, cellDimensions, isPending = false) {

    const strokeWidth = isPending ? 3 : 1;

    const pathData = path();
    pathData.moveTo(cellDimensions.l, cellDimensions.t);
    pathData.lineTo(cellDimensions.r, cellDimensions.b);

    if (! isPending) {
        pathData.moveTo(cellDimensions.l, cellDimensions.b);
        pathData.lineTo(cellDimensions.r, cellDimensions.t);
    }

    selection
        .append('path')
        .attr('d', pathData)
        .attr('stroke', '#ddd')
        .attr('stroke-width', strokeWidth);
}


function calcOverrideCellDimensions(cellPadding) {
    const xBandwidth = scales.x.bandwidth();
    const yBandwidth = scales.y.bandwidth();
    return {
        t: cellPadding,
        l: cellPadding,
        b: yBandwidth - cellPadding,
        r: xBandwidth - cellPadding,
        w: xBandwidth - (2 * cellPadding),
        h: yBandwidth - (2 * cellPadding)
    }
}


function calculateCellTranslation(d) {
    const translation = {
        dx: scales.x(d.measurableX),
        dy: scales.y(d.measurableY),
    };
    return `translate(${ translation.dx }, ${ translation.dy })`;
}


function toCellId(override) {
    return `${override.measurableX}_${override.measurableY}_${override.rating}`;
}


function drawExistingOverrides(selection, overrides = [], colorScale) {
    const cellPadding = 3;
    const cellDimensions = calcOverrideCellDimensions(cellPadding);

    const drawBoxes = selection => {
        return selection
            .append('rect')
            .classed('cell-override', true)
            .attr('x', cellDimensions.l)
            .attr('y', cellDimensions.t)
            .attr('width', cellDimensions.w)
            .attr('height', cellDimensions.h)
            .attr('fill', d => colorScale(d.rating).brighter(1.3))
            ;
    };

    const overrideElems = selection
        .selectAll('.existing-override')
        .data(
            _.values(overrides),
            toCellId);

    const newOverrides = overrideElems
        .enter()
        .append('g')
        .classed('existing-override', true);

    overrideElems
        .exit()
        .remove();

    newOverrides
        .attr('transform', calculateCellTranslation)
        .call(drawBoxes)
        .filter(d => d.rating === 'X')
        .call(drawXShape, cellDimensions);
}


function drawPendingOverrideShape(selection, cellDimensions, colorScale) {
    const notchSizeX = cellDimensions.w / 1.5;
    const notchSizeY = cellDimensions.h / 1.5;
    const pathData = path();
    pathData.moveTo(cellDimensions.l, cellDimensions.t);
    pathData.lineTo(cellDimensions.r - notchSizeX, cellDimensions.t);
    pathData.lineTo(cellDimensions.r, cellDimensions.t + notchSizeY);
    pathData.lineTo(cellDimensions.r, cellDimensions.b);
    pathData.lineTo(cellDimensions.l + notchSizeX, cellDimensions.b);
    pathData.lineTo(cellDimensions.l, cellDimensions.b - notchSizeY);
    pathData.closePath();

    selection
        .append('path')
        .classed('cell-override', true)
        .attr('d', pathData)
        .attr('fill', d => colorScale(d.rating).brighter(1.3))
        ;
}


function drawPendingOverrides(selection, overrides, colorScale) {
    const cellPadding = 3;
    const cellDimensions = calcOverrideCellDimensions(cellPadding);

    const overrideElems = selection
        .selectAll('.pending-override')
        .data(
            _.values(overrides),
            toCellId);

    const newOverrides = overrideElems
        .enter()
        .append('g')
        .classed('pending-override', true);

    overrideElems
        .exit()
        .remove();

    newOverrides
        .attr('transform', calculateCellTranslation)
        .call(drawPendingOverrideShape, cellDimensions, colorScale)
        .filter(d => d.rating === 'X')
        .call(drawXShape, cellDimensions, true);
}


function drawInherited(selection, perspective) {
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
        .attr('fill', d => perspective.ratingColorScales.x(d.col.rating.rating).brighter(2))
        ;

    const drawRowTriangles = (selection) => selection
        .append('polyline')
        .classed('cell-inherited', true)
        .attr('points', `${tl} ${bl} ${br}`)
        .attr('stroke', '#ccc')
        .attr('fill', d => perspective.ratingColorScales.y(d.row.rating.rating).brighter(2))
        ;

    selection
        .call(drawColTriangles)
        .call(drawRowTriangles)
        ;
}


function drawRowTitles(elem, perspective) {
    const drawRect = (selection) => selection
        .append('rect')
        .attr('x', (labelIndicatorPadding + labelIndicatorSize) * -1)
        .attr('y', d => scales.y(d.measurable.id) + labelIndicatorPadding)
        .attr('width', labelIndicatorSize)
        .attr('height', scales.y.bandwidth() - labelIndicatorPadding * 2)
        .attr('fill', d => perspective.ratingColorScales.y(d.rating.rating).brighter(1.5))
        .attr('stroke', d => perspective.ratingColorScales.y(d.rating.rating))
        ;

    const drawText = (selection) => selection
        .append('text')
        .classed('row-title', true)
        .text(d => truncateMiddle(d.measurable.name, maxLabelLength))
        .attr('text-anchor', 'end')
        .attr('transform', d => `translate(-${labelPadding},  ${scales.y(d.measurable.id) + scales.y.bandwidth() / 2})`)
        .filter(d => d.measurable.name.length > maxLabelLength)
        .append('title')
        .text(d => d.measurable.name)
        ;

    const rowTitles = elem
            .selectAll('.row-titles')
            .selectAll('.row-title')
            .data(perspective.axes.y, d => d.measurable.id)
        ;

    const newTitles = rowTitles
        .enter()
        .append('g')
        .classed('row-title', true)
        ;

    newTitles.call(drawRect);
    newTitles.call(drawText);
}


function drawColTitles(elem, perspective) {
    const colTitles = elem
        .selectAll('.col-titles')
        .selectAll('.col-title')
        .data(perspective.axes.x, d => d.measurable.id)
        ;

    const drawRect = (selection) => selection
        .append('rect')
        .attr('x', d => scales.x(d.measurable.id) + labelIndicatorPadding)
        .attr('y', (labelIndicatorPadding + labelIndicatorSize) * -1)
        .attr('width', scales.x.bandwidth() - labelIndicatorPadding * 2)
        .attr('height', labelIndicatorSize)
        .attr('fill', d => perspective.ratingColorScales.x(d.rating.rating).brighter(1.5))
        .attr('stroke', d => perspective.ratingColorScales.x(d.rating.rating))
        ;

    const drawText = selection => selection
        .append('text')
        .classed('col-title', true)
        .text(d => truncateMiddle(d.measurable.name, maxLabelLength))
        .attr('text-anchor', 'start')
        .attr('transform', d =>
            `translate(${scales.x(d.measurable.id) + scales.x.bandwidth() / 3}, -${labelPadding}) rotate(-20)`)
        .filter(d => d.measurable.name.length > maxLabelLength)
        .append('title')
        .text(d => d.measurable.name)
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
            },
            onCellOver: function(d) {
            }
        },
        custom: customHandlers
    };
}


function setupDimensions(perspective) {
    width = cellWidth * perspective.axes.x.length;
    height = cellHeight * perspective.axes.y.length;
}


function canDraw(d = {}) {
    const checks = [
        d => d.measurables.length > 0,
        d => d.measurableRatings.length > 0,
        d => d.perspectiveDefinition != null,
        d => d.schemes != null]
    return _.every(checks, check => check(d));
}


function controller($element, $q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function attemptDraw() {

        if (canDraw(vm)) {
            vm.perspective = mkPerspective(
                vm.perspectiveDefinition,
                vm.measurables,
                vm.measurableRatings,
                vm.schemes);

            setupDimensions(vm.perspective);
            setupScales(vm.perspective);

            vm.svg = initSvg($element, width, height);
            const handlers = initHandlers(vm.svg, vm.handlers);

            draw(vm.svg, vm.perspective, handlers);
            vm.svg.call(drawExistingOverrides, vm.existingOverrides, vm.perspective.ratingColorScales.p);
            vm.svg.call(drawPendingOverrides, vm.pendingOverrides, vm.perspective.ratingColorScales.p);
        }

        const canRefresh = (vm.existingOverrides || vm.pendingOverrides)
            && vm.svg && canDraw;

        if (canRefresh) {
            vm.svg.call(drawExistingOverrides, vm.existingOverrides, vm.perspective.ratingColorScales.p);
            vm.svg.call(drawPendingOverrides, vm.pendingOverrides, vm.perspective.ratingColorScales.p);
        }
    }

    function loadSchemes(perspectiveDefinition) {
        const schemePromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => _.keyBy(r.data, 'id'));
        const categoryPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => _.keyBy(r.data, 'id'));

        return $q
            .all([schemePromise, categoryPromise])
            .then(([schemesById, categoriesById]) => {
                const xCat = categoriesById[perspectiveDefinition.categoryX];
                const yCat = categoriesById[perspectiveDefinition.categoryY];
                return {
                    x: schemesById[xCat.ratingSchemeId],
                    y: schemesById[yCat.ratingSchemeId],
                    p: schemesById[perspectiveDefinition.ratingSchemeId]
                };
            });

    }

    vm.$onChanges = (c) => {
        if (vm.perspectiveDefinition) {
            loadSchemes(vm.perspectiveDefinition)
                .then((schemes) => {
                    vm.schemes = schemes;
                    attemptDraw();
                });
        }
        attemptDraw();
    };
}


controller.$inject = [
    '$element',
    '$q',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default component;