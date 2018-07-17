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

import {initialiseData} from '../../../common';
import {lineWithArrowPath, responsivefy} from '../../../common/d3-utils';
import {event, select} from 'd3-selection';
import {forceCenter, forceCollide, forceLink, forceManyBody, forceSimulation} from 'd3-force';
import {drag} from 'd3-drag';
import {symbol, symbolWye} from 'd3-shape';
import {zoom, zoomIdentity} from 'd3-zoom';
import 'd3-selection-multi';
import _ from "lodash";


import template from './boingy-graph.html';

const width = 800;
const height = 500;


const bindings = {
    data: '<',
    tweakers: '<'
};


const initialState = {
    zoomEnabled: false
};


const DEFAULT_TWEAKER = {
    enter: (selection) => selection,
    exit: (selection) => selection,
    update: (selection) => selection
};


const simulation = forceSimulation()
    .velocityDecay(0.5)
    .force("center", forceCenter(width / 2, height / 2))
    .force("charge", forceManyBody().strength(-110).distanceMin(1).distanceMax(400))
    .force("collide", forceCollide())
    .force("link", forceLink().id(d => d.id).iterations(2).distance(55)); //.strength(0.2));

const actorSymbol = symbol()
    .size(128)
    .type(symbolWye);


function mkLinkData(flows = []) {
    return _.chain(flows)
        .map(f => ({
            id: `${ f.source.id }_${ f.target.id }`,
            source: f.source.id,
            target: f.target.id,
            data: f
        }))
        .uniqBy('id')
        .value();
}


function drawLinks(links = [], holder, tweakers) {
    const linkSelection = holder
        .selectAll(".wdfd-link")
        .data(links, d => d.id);

    const newLinks = linkSelection
        .enter()
        .append("path")
        .classed('wdfd-link', true)
        .attr('stroke', '#444')
        .attr('stroke-opacity', 0.6)
        .call(tweakers.enter);

    linkSelection
        .exit()
        .remove();

    return linkSelection
        .merge(newLinks)
        .call(tweakers.update);
}


function addNodeLabel(selection) {
    selection
        .append('text')
        .attr('dx', 9)
        .attr('dy', '.35em')
        .text(d => d.name);
}


function addNodeCircle(selection) {
    selection
        .filter(d => d.kind === 'APPLICATION')
        .append('circle')
        .attr("class", "wdfd-glyph")
        .attr("r", 6);

    selection
        .filter(d => d.kind === 'ACTOR')
        .append("path")
        .attr("class", "wdfd-glyph")
        .attr("d", actorSymbol);

}


function drawNodes(nodes = [], holder, tweakers = DEFAULT_TWEAKER) {

    function dragStarted(d) {
        if (!event.active) {
            simulation
               .alphaTarget(0.1)
               .restart();
        }
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    function dragEnded(d) {
        if (!event.active) {
            simulation
                .alphaTarget(0)
                .restart();
        }
        d.fx = event.x;
        d.fy = event.y;
    }

    const nodeSelection = holder
        .selectAll(".wdfd-node")
        .data(nodes, d => d.id);

    const newNodes = nodeSelection
        .enter()
        .append('g')
        .classed('wdfd-node', true)
        .on('dblclick.unfix', d => { d.fx = null; d.fy = null })
        .call(drag()
            .on("start", dragStarted)
            .on("drag", dragged)
            .on("end", dragEnded));

    newNodes
        .call(addNodeLabel)
        .call(addNodeCircle)
        .call(tweakers.enter)

    nodeSelection
        .exit()
        .call(tweakers.exit)
        .remove();

    return nodeSelection
        .merge(newNodes);

}


function setup(vizElem) {
    const svg = vizElem
        .append('svg')
        .attr("width", width)
        .attr("height", height)
        .attr('viewBox', `0 0 ${width} ${height}`);

    const destroyResizeListener = responsivefy(svg);

    svg.append("g")
        .attr("class", "links");

    svg.append("g")
        .attr("class", "nodes");

    return { svg, destroyResizeListener };
}



function draw(data = [],
              parts,
              tweakers = {}) {

    const linkTweakers = _.defaults(tweakers.link, DEFAULT_TWEAKER);
    const nodeTweakers = _.defaults(tweakers.node, DEFAULT_TWEAKER);

    const links = mkLinkData(data.flows);
    const nodes = data.entities;

    const linkSelection = drawLinks(
        links,
        parts.svg.select('.links'),
        linkTweakers);

    const nodeSelection = drawNodes(
        nodes,
        parts.svg.select('.nodes'),
        nodeTweakers);

    const ticked = () => {
        nodeSelection
            .attr('transform', d => `translate(${d.x}, ${d.y})`);

        linkSelection
            .call(lineWithArrowPath);
    };

    simulation
        .nodes(nodes)
        .on("tick", ticked);

    simulation
        .force("link")
        .links(links);


    return simulation;
}


function controller($timeout, $element) {
    const vm = initialiseData(this, initialState);

    const vizElem = select($element[0])
        .select('.viz');

    const parts = setup(vizElem);

    const debouncedDraw = _.debounce(() => {
        draw(vm.data, parts, vm.tweakers);
        simulation.alpha(0.4).restart();
    }, 250);

    vm.$onChanges = (changes) => {
        if (changes.data) {
            // we draw using async to prevent clientWidth reporting '0'
            $timeout(debouncedDraw);
        }
    };

    vm.$onInit = () => {
        simulation.restart();
    };

    vm.$onDestroy = () => {
        simulation.stop();
        parts.destroyResizeListener();
    };

    function zoomed() {
        const svg = vizElem
            .select("svg");

        const t = event.transform;

        svg.select('.nodes')
            .attr("transform", t);
        svg.select('.links')
            .attr("transform", t);
    }

    const myZoom = zoom()
        .scaleExtent([1 / 4, 2])
        .on("zoom", zoomed);

    vm.enableZoom = () => {
        vizElem
            .select("svg")
            .call(myZoom);
        vm.zoomEnabled = true;
    };

    vm.disableZoom = () => {
        vizElem
            .select("svg")
            .on('.zoom', null);

        vm.zoomEnabled = false;
    };

    vm.resetSimulation = () => {
        _.forEach(vm.data.entities, d => {
            d.x = null;
            d.y = null;
            d.fx = null;
            d.fy = null;
        });

        vizElem
            .select('svg')
            .transition()
            .duration(750)
            .call(myZoom.transform, zoomIdentity);

        debouncedDraw();
    };
}


controller.$inject = ['$timeout', '$element'];


const component = {
    bindings,
    template,
    controller
};


export default component;