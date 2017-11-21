

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

import _ from 'lodash';
import { select } from 'd3-selection';
import { scaleBand, scaleLinear } from 'd3-scale';


const bindings = {
    chart: '<'
};


const initialState = {

};

const margin = {top: 20, right: 10, bottom: 20, left: 10};
const totalWidth = 1024;
const width = totalWidth - margin.left - margin.right;

const blocks = 32;
const blockHeight = 20;
const blockWidth = width / blocks;


const regionInfo = [
    {
        name: 'xHeader',
        x: 5,
        y: 1,
        w: blocks - 5
    }, {
        name: 'yHeader',
        x: 1,
        y: 1,
        w: 4
    }, {
        name: 'xHistory',
        x: 5,
        y: 0,
        w: blocks - 5
    }, {
        name: 'yHistory',
        x: 0,
        y: 2,
        w: 1
    }, {
        name: 'rowGroups',
        x: 1,
        y: 2,
        w: blocks - 1
    }
];


function initRegions(svg, scaleX) {
    const mkGroup = (info) => {
        const g = svg
            .append('g')
            .classed(`${info.name}`, true)
            .attr('transform', `translate(${scaleX(info.x)}, ${blockHeight * info.y})`);

        g.append('rect')
            .attr('width', info.w * blockWidth)
            .attr('height', blockHeight)
            .attr('stroke', '#ccc')
            .attr('fill', '#fff')
            .attr('opacity', 0.1);

        g.append('text')
            .text(info.name)
            .attr('x', (info.w * blockWidth) / 3)
            .attr('y', blockHeight)
            .attr('fill', '#ccc')
            .attr('opacity', 0.5);

        return g;
    };

    return _.reduce(
        regionInfo,
        (acc, r) => {
            acc[r.name] = Object.assign({}, r, { g: mkGroup(r)});
            return acc;
        },
        { svg });
}


function drawRowGroups(region, chartData, colScale) {

    const yOffsets = _.reduce(
        chartData.rows.groups,
        (acc, g) => {
            const appCount = g.applications.length;
            acc[g.domain.id] = acc.total;
            acc.total = acc.total + (appCount * blockHeight);
            return acc;
        },
        { total: 0 });

    const rowGroups = region.g
        .selectAll('.rowGroup')
        .data(chartData.rows.groups, d => d.domain.id);

    const newRowGroups = rowGroups
        .enter()
        .append('g')
        .attr('transform', (d, i) => `translate(0, ${yOffsets[d.domain.id]})`);


    newRowGroups
        .append('text')
        .attr('y', blockHeight)
        .text(d => d.domain.name);

    newRowGroups
        .append('g')
        .attr('transform', `translate(${blockWidth * 3}, 0)`)
        .selectAll('.appRow')
        .data(d => d.applications)
        .enter()
        .append('text')
        .text(d => d.name)
        .attr('y', (d,i) => (i + 1) * blockHeight)


}


function drawColHeaders(region, cols, colScale) {
    const headers = region.g
        .selectAll('.colHeader')
        .data(cols.domain, d => d.id)
        .enter()
        .append('g')
        .classed('colHeader', true)
        .attr('transform', d => `translate(${colScale(d.id)}, 0)`);

    headers
        .append('rect')
        .attr('fill', '#f8f6d5')
        .attr('y', 0)
        .attr('width', colScale.bandwidth())
        .attr('height', blockHeight);

    headers
        .append('text')
        .text(d => d.name)
        .attr('x', colScale.bandwidth() / 2)
        .attr('text-anchor', 'middle')
        .attr('y', 16)
}


function draw(chartData, regions, blockScaleX) {
    console.log('draw', chartData.cols.domain.length, regions);

    const colScale = scaleBand()
        .domain(_.map(chartData.cols.domain, 'id'))
        .range([0, blockScaleX(regions.xHeader.w)])
        .paddingInner([0.1])
        .paddingOuter([0.3])
        .align([0.5]);


    drawColHeaders(regions.xHeader, chartData.cols, colScale);
    drawRowGroups(regions.rowGroups, chartData, colScale);
}




function calcTotalRequiredHeight(chartData) {
    const top = blockHeight * 2;
    const groups = _
        .chain(chartData.rows.groups)
        .map(g => g.applications.length)
        .sum()
        .value() * blockHeight;

    const total = top + groups + margin.top + margin.bottom;

    return total;
}


function controller($element, $timeout) {
    const vm = initialiseData(this, initialState);

    let regions = null;

    vm.$onInit = () => {
        const rootElem = $element[0];

        $timeout(() => {
            const height = calcTotalRequiredHeight(vm.chart);
            const svg = select(rootElem)
                .select('svg')
                .attr('viewBox', `0 0 1024 ${height}`);

            const blockScaleX = scaleLinear().domain([0,32]).range([0, width]);

            regions = initRegions(svg, blockScaleX);

            draw(vm.chart, regions, blockScaleX);
        }, 1000);

        global.chart = vm.chart;
    };

    vm.$onChanges = () => {
        if (regions) {
            $timeout(() => draw(vm.chart, regions), 1000);
        }
    };
}


controller.$inject = ['$element', '$timeout'];


const component = {
    controller,
    bindings,
    template
};


export default {
    id: 'waltzAuthSourcesNavigatorChart',
    component
}