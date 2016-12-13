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

import {select} from 'd3-selection';
import {rgb} from 'd3-color';

const bindings = {};


const template = require('./common-svg-defs.html');


function controller($element) {

    const defs = select($element[0])
        .select('svg')
        .append('defs');

    const markers = [
        { name: 'arrowhead' , color: '#666' },
        { name: 'arrowhead-PRIMARY' , color: 'green' },
        { name: 'arrowhead-SECONDARY' , color: 'orange' },
        { name: 'arrowhead-DISCOURAGED' , color: 'red' },
        { name: 'arrowhead-NO_OPINION' , color: '#333' }
    ];

    defs.selectAll('marker')
        .data(markers, m => m.name)
        .enter()
        .append('marker')
        .attr('id', d => d.name)
        .attr('refX', 20)
        .attr('refY', 4)
        .attr('markerUnits', "strokeWidth")
        .attr('markerWidth', 8)
        .attr('markerHeight', 8)
        .attr('orient', 'auto')
        .attr('stroke', d => rgb(d.color).darker(0.5).toString())
        .attr('fill', d => rgb(d.color).brighter(1.5).toString())
        .append('path')
        .attr('d', 'M 0,0 V 8 L8,4 Z'); // this is actual shape for arrowhead
}


controller.$inject= ['$element'];


const component = {
    template,
    controller,
    bindings
};

export default component;