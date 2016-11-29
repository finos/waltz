/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import {initialiseData} from '../../../common';
import {event, select} from 'd3-selection';
import {forceSimulation, forceLink, forceManyBody, forceCenter} from 'd3-force';
import {drag} from 'd3-drag';
import {zoom, zoomIdentity} from 'd3-zoom';

import {markerFix} from '../../../common';


import 'd3-selection-multi';

import _ from "lodash";


const template = require('./boingy-graph.html');


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
    .force("charge", forceManyBody().strength(-50))
    .force("link", forceLink().id(d => d.id).iterations(2).distance(100)); //.strength(0.2));


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


function setupDimensions(vizElem) {
    const width = vizElem.node().clientWidth || 1000;
    return { width, height: 600 };
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
        .attrs({ dx: 10, dy: '.35em' })
        .text(d => d.name);
}


function addNodeCircle(selection) {
    selection
        .append('circle')
        .attr("r", 8)
        .append("title")
        .text(d => d.name);
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
        .call(tweakers.enter)
        .call(drag()
            .on("start", dragStarted)
            .on("drag", dragged)
            .on("end", dragEnded));

    newNodes
        .call(addNodeLabel)
        .call(addNodeCircle);

    nodeSelection
        .exit()
        .call(tweakers.exit)
        .remove();

    return nodeSelection
        .merge(newNodes);

}


function setup(vizElem) {
    const svg = vizElem
        .append('svg');

    svg.append("g")
        .attr("class", "links");

    svg.append("g")
        .attr("class", "nodes");

    return { svg, vizElem };
}

function draw(data = [],
              parts,
              tweakers = {}) {

    const linkTweakers = _.defaults(tweakers.link, DEFAULT_TWEAKER);
    const nodeTweakers = _.defaults(tweakers.node, DEFAULT_TWEAKER);

    const dimensions = setupDimensions(parts.vizElem);
    parts
        .svg
        .attrs({
            width: dimensions.width,
            height: dimensions.height
        });

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

    simulation
        .force("center", forceCenter(dimensions.width / 2, dimensions.height / 2));

    simulation
        .nodes(nodes)
        .on("tick", ticked);

    simulation
        .force("link")
        .links(links);

    function ticked() {
        nodeSelection
            .attr('transform', d => `translate(${d.x}, ${d.y})`);

        linkSelection
            .attr("d", function(d) {
                const dx = d.target.x - d.source.x;
                const dy = d.target.y - d.source.y;
                const dr = Math.sqrt(dx * dx + dy * dy);
                const theta = Math.atan2(dy, dx); //+ Math.PI / 7.85,  // (rotate marker)
                const d90 = Math.PI / 2;
                const dtxs = d.target.x - 16 * Math.cos(theta);  // val is how far 'back'
                const dtys = d.target.y - 16 * Math.sin(theta);
                return `M${d.source.x},${d.source.y} 
                        l${dx} ${dy}
                        M${dtxs},${dtys}
                        l${(3.5 * Math.cos(d90 - theta) - 10 * Math.cos(theta))}
                            ,${(-3.5 * Math.sin(d90 - theta) - 10 * Math.sin(theta))}
                        L${(dtxs - 3.5 * Math.cos(d90 - theta) - 10 * Math.cos(theta))}
                            ,${(dtys + 3.5 * Math.sin(d90 - theta) - 10 * Math.sin(theta))}
                        z`;
            });

    }

    return simulation;
}


function controller($scope, $element) {
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
            $scope.$applyAsync(debouncedDraw);
        }
    };

    vm.$onInit = () => {
        simulation.restart();
    };

    vm.$onDestroy = () => {
        if (simulation) {
            simulation.stop();
        }
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
        .scaleExtent([1 / 4, 1.5])
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
    }
}


controller.$inject = ['$scope', '$element'];


const component = {
    bindings,
    template,
    controller
};


export default component;