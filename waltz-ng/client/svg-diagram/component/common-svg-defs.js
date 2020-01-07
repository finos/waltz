/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import {select} from 'd3-selection';
import {rgb} from 'd3-color';
import template from './common-svg-defs.html';

const bindings = {};


function addArrowMarkers(defs) {
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


function addGlowFilter(defs) {
    // taken from: https://www.visualcinnamon.com/2016/06/glow-filter-d3-visualization.html
    const filter = defs
        .append("filter")
        .attr("id","waltz-glow");
    filter
        .append("feGaussianBlur")
        .attr("stdDeviation","3.5")
        .attr("result","coloredBlur");

    const feMerge = filter
        .append("feMerge");
    feMerge
        .append("feMergeNode")
        .attr("in","coloredBlur");
    feMerge
        .append("feMergeNode")
        .attr("in","SourceGraphic");
}


const outlineMatrix = `
     0 0 0 0 0
     0 0 0 0 0
     0 0 0 0 0
     0 0 0 1 0`;


function addOutliner(defs) {
    const filter = defs
        .append("filter")
        .attr("id","waltz-outline");

    filter.append('feFlood')
        .attr('flood-color', '#fff')
        .attr('result', 'base');

    filter.append('feMorphology')
        .attr('result', 'bigger')
        .attr('in', 'SourceGraphic')
        .attr('operator', 'dilate')
        .attr('radius', 3);

    filter.append('feColorMatrix')
        .attr('result', 'mask')
        .attr('in', 'bigger')
        .attr('type', 'matrix')
        .attr('values', outlineMatrix);

    filter.append('feComposite')
        .attr('result', 'drop')
        .attr('in', 'base')
        .attr('in2', 'mask')
        .attr('operator', 'in');

    filter.append('feGaussianBlur')
        .attr('result', 'blur')
        .attr('in', 'drop')
        .attr('stdDeviation', 3);

    filter.append('feBlend')
        .attr('in', 'SourceGraphic')
        .attr('in2', 'blur')
        .attr('mode', 'normal');
}


function controller($element) {

    const defs = select($element[0])
        .select('svg')
        .append('defs');

    addArrowMarkers(defs);
    addGlowFilter(defs);
    addOutliner(defs);
}


controller.$inject= ['$element'];


const component = {
    template,
    controller,
    bindings
};

export default component;