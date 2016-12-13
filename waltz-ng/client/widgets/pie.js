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

/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */
import {select} from 'd3-selection';
import {pie, arc} from 'd3-shape';
import "d3-selection-multi";

import _ from "lodash";


/**
 * data: [....]
 *
 * config: {
 *   colorProvider,
 *   valueProvider : d => d.count,
 *   labelProvider : d => ''
 * }
 */

const bindings = {
    data: '<',
    config: '<',
    selectedSegmentKey: '<'
};


const defaultOnSelect = (d) => console.log("pie.onSelect default handler: ", d);


const DEFAULT_SIZE = 70;


function renderArcs(holder, config, data, onSelect) {

    const {
        colorProvider,
        valueProvider = (d) => d.count,
        idProvider = (d) => d.data.key,
        size = DEFAULT_SIZE
    } = config;

    const radius = size / 2;

    const pieArc = arc()
        .outerRadius(radius - 10)
        .innerRadius(0);

    const pieLayout = pie()
        .value(valueProvider);

    const pieData = pieLayout(_.filter(data, r => r.count > 0));

    const arcs = holder
        .selectAll('.arc')
        .data(pieData, idProvider);

    const newArcs = arcs
        .enter()
        .append('path')
        .classed('arc clickable', true)
        .on('click', d => onSelect(d.data));

    newArcs
        .append('title')
        .text(d => `${d.data.key} - ${d.data.count}`);

    arcs
        .merge(newArcs)
        .attr('d', d => pieArc(d))
        .attr("fill", d => colorProvider(d).brighter())
        .attr("stroke", d => colorProvider(d));

    arcs.exit()
        .remove();

    const emptyPie = holder
        .selectAll('.empty-pie')
        .data(data.length ? [] : [1]);

    emptyPie
        .enter()
        .append('circle')
        .attrs({
            cx: 0,
            cy: 0,
            r: radius / 2,
            fill: '#eee',
            stroke: '#bbb',
            'stroke-dasharray': [5, 1]
        })
        .classed('empty-pie', true);

    emptyPie
        .exit()
        .remove();
}


function render(svg, config, data, onSelect) {
    const { size = DEFAULT_SIZE } = config;
    const width = size;
    const height = size;

    svg.attrs( { width, height });

    const mainGroup = svg
        .selectAll('.main-group')
        .data([1]);

    const newMainGroup = mainGroup
        .enter()
        .append('g')
        .classed('main-group', true);

    const g = newMainGroup
        .merge(mainGroup)
        .attr('transform', `translate(${width / 2},${height / 2})`);

    renderArcs(g, config, data, onSelect);
}


function controller($element, $scope) {
    const vizElem = $element[0].querySelector('.waltz-pie');

    const svg = select(vizElem)
        .append('svg');

    const vm = this;

    vm.$onChanges = (changes) => {
        if (vm.data && vm.config && changes.data) {
            const onSelectFn = vm.config.onSelect || defaultOnSelect;
            const onSelect = (d) => $scope.$apply(() => onSelectFn(d));
            render(svg, vm.config, vm.data, onSelect);
        }

        if (changes.selectedSegmentKey) {
            svg.selectAll('.arc')
                .classed('wp-selected', d => {
                    return d.data.key === vm.selectedSegmentKey;
                });
        }
    };


}


controller.$inject = [
    '$element',
    '$scope'
];


const component = {
    bindings,
    controller,
    template: require('./pie.html')
};


export default component;
