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

import _ from "lodash";
import { CORE_API } from "../../common/services/core-api-utils";
import { initialiseData } from "../../common";
import { buildHierarchies } from "../../common/hierarchy-utils";
import { select } from "d3-selection";

import template from "./dynamic-nav-aid.html";
import { rgb } from 'd3-color';


const bindings = {
};


const initialState = {};


const baseDimensions = {
    graph: {
        width: 1000,
        height: 600,
    },
    margin: {
        top: 40,
        left: 5,
        right: 5,
        bottom: 50
    },
    label: {
        height: 10,
        minSpacing: 8,
        width: 200
    },
    header: {
        height: 20
    }
};


let redraw = null;


function prepareGraph(svg) {
    const header = svg
        .append("g")
        .classed("dna-header", true);

    const topBlocks = svg
        .append("g")
        .classed("dna-top-blocks", true);

    return {
        header,
        svg,
        topBlocks
    };
}


function update(sections,
                model,
                tweakers) {
    redraw = () => update(sections, model, tweakers);

    // drawBlocks(sections.topBlocks, model);

    const blocks = sections.topBlocks
        .selectAll('.dna-block')
        .data(model, d => d.id);

    drawNestedBlocks(blocks);
}


function drawNestedBlocks(selection) {
    console.log('drawNestedBlocks: ', selection);

    const newBlockGs = selection
        .enter()
        .append('g')
        .classed('dna-block', true)
        .attr("transform", (d, i) => `translate(${i * 100} 0)`);

    const fill = rgb('#6d84ff');

    newBlockGs
        .append('rect')
        .attrs((d, i) => {
            console.log({d, i});
            return {
                fill,
                stroke: '#ccc',
                width: 100,
                height: 100
            }
        });


    newBlockGs
        .append('rect')
        .attrs((d, i) => {
            return {
                fill: fill.darker(1.5),
                stroke: '#ccc',
                width: 100,
                height: 25
            }
        });


    newBlockGs
        .append("text")
        .attrs((d,i) => {
            return {
                fill: "#FFF",
                dy: 12,
                dx: 25
            };
        })
        .text(d => d.name);

    const children = newBlockGs
        .selectAll('.dna-block')
        .data((d) => d.children, d => d.id)
        .enter();

    // children.each(d => console.log('each: ', d));

    // console.log('children: ', children, children.empty())
    if (!children.empty()) {
        children.each(d => drawNestedBlocks(d));;
    }
}


function drawBlocks(section, model) {
    console.log('section: ', section)

    const blocks = section
        .selectAll('.dna-block')
        .data(model, d => d.id);

    // enter
    const newBlockGs = blocks
        .enter()
        .append('g')
        .classed('dna-block', true)
        .attr("transform", (d, i) => `translate(${i * 100} 0)`);


    const fill = rgb('#6d84ff');

    const blockRects = newBlockGs
        .append('rect')
        .attrs((d, i) => {
            console.log({d, i});
            return {
                fill,
                stroke: '#ccc',
                width: 100,
                height: 100
            }
        });


    const titleRects = newBlockGs
        .append('rect')
        .attrs((d, i) => {
            console.log({d, i});
            return {
                fill: fill.darker(1.5),
                stroke: '#ccc',
                width: 100,
                height: 25
            }
        });


    newBlockGs
        .append("text")
        .attrs((d,i) => {
            return {
                fill: "#FFF",
                dy: 12,
                dx: 25
            };
        })
        .text(d => d.name);

    newBlockGs
        .selectAll('.dna-block')
        .data((d) => d.children)
        .enter()
        .append('g')
        .call((d, i) => {
            console.log('call child: ', d, i, d.datum());
            drawBlocks(d)
        })
        // .classed('dna-block', true)
        // .attr("transform", (d, i) => `translate(${i * 100} 0)`);

    // update
    // TBD - not likely to be needed


    // exit
    // TBD - not likely to be needed
}



function controller($element, $window, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const svg = select($element.find("svg")[0]);
    const svgSections = prepareGraph(svg);


    const render = () => {
        // baseDimensions.graph.width = $element
        //     .parent()[0]
        //     .clientWidth;
        console.log(baseDimensions.graph.width)

        svgSections.svg
            .attr('width', baseDimensions.graph.width)
            .attr('height', baseDimensions.graph.height);

        update(svgSections, vm.dataTypeHierarchy);
    };

    const debouncedRender = _.debounce(render, 100);

    const tree = data => d3.tree()
        .size([2 * Math.PI, radius])
        .separation((a, b) => (a.parent == b.parent ? 1 : 2) / a.depth)
        (d3.hierarchy(data))

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(CORE_API.DataTypeStore.findAll, [])
            .then(r => vm.dataTypes = r.data)
            .then(() => vm.dataTypeHierarchy = buildHierarchies(vm.dataTypes, false))
            .then(() => {
                console.log(vm.dataTypeHierarchy);

            });

        angular
            .element($window)
            .on('resize', debouncedRender);
    };

    vm.$onChanges = (changes) => debouncedRender();

    vm.$onDestroy = () => angular
        .element($window)
        .off('resize', debouncedRender);
}


controller.$inject = [
    "$element",
    "$window",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzDynamicNavAid"
};
