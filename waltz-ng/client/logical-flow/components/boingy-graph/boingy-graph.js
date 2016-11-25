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

import {event, select} from 'd3-selection';
import {forceSimulation, forceLink, forceManyBody, forceCenter} from 'd3-force';
import {drag} from 'd3-drag';
import {zoom} from 'd3-zoom';

import 'd3-selection-multi';

import _ from "lodash";


const template = require('./boingy-graph.html');


const bindings = {
    data: '<',
    tweakers: '<'
};


const DEFAULT_TWEAKER = {
    enter: (selection) => selection,
    exit: (selection) => selection,
    update: (selection) => selection
};


function mkLinkData(flows = []) {
    return _.map(
        flows,
        f => ({
            source: f.source.id,
            target: f.target.id,
            data: f
        }));
}


function setupDimensions(vizElem) {
    const width = vizElem.node().clientWidth || 1000;
    return { width, height: 600 };
}


function drawLinks(links = [], holder, tweakers) {
    const linkSelection = holder
        .selectAll(".wdfd-link")
        .data(links, d => d.data.id);

    const newLinks = linkSelection
        .enter()
        .append("line")
        .classed('wdfd-link', true)
        .attr('stroke', '#666')
        .call(tweakers.enter);

    linkSelection
        .exit()
        .call(tweakers.exit)
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


function drawNodes(nodes = [], holder, simulation, tweakers = DEFAULT_TWEAKER) {

    function dragStarted(d) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    function dragEnded(d) {
        if (!event.active) simulation.alphaTarget(0);
        // d.fx = null;
        // d.fy = null;

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
        .merge(newNodes)
        .call(tweakers.update);

}


function setup(vizElem) {
    const svg = vizElem
        .append('svg');

    const nG = svg.append("g")
        .attr("class", "nodes");

    const lG = svg.append("g")
        .attr("class", "links");

    const myZoom = zoom()
        .scaleExtent([1 / 4, 1.5])
        .on("zoom", zoomed);

    svg.call(myZoom);

    //svg.on(".zoom", null);

    function zoomed() {
        const t = event.transform;

        nG.attr("transform", t);
        lG.attr("transform", t);
    }

    return { svg, vizElem };
}



function draw(data,
              parts,
              tweakers = {},
              simulation) {

    const linkTweakers = _.defaults(tweakers.link, DEFAULT_TWEAKER);
    const nodeTweakers = _.defaults(tweakers.node, DEFAULT_TWEAKER);

    const links = mkLinkData(data.flows);

    const dimensions = setupDimensions(parts.vizElem);
    parts.svg.attrs({
        width: dimensions.width,
        height: dimensions.height
    });

    simulation
        .force("center", forceCenter(dimensions.width / 2, dimensions.height / 2));

    const linkSelection = drawLinks(
        links,
        parts.svg.select('g.links'),
        linkTweakers);

    const nodeSelection = drawNodes(
        data.entities,
        parts.svg.select('g.nodes'),
        simulation,
        nodeTweakers);

    simulation
        .nodes(data.entities)
        .on("tick", ticked);

    simulation
        .force("link")
        .links(links);

    // kick the simulation so new nodes are drawn/moved
    simulation
        .alpha(0.2)
        .restart();

    function ticked() {
        linkSelection
            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; })
            .call(linkTweakers.update);

        nodeSelection
            .attr('transform', d => `translate(${d.x}, ${d.y})`);

    }

}


function controller($scope, $element) {
    const vm = this;

    const vizElem = select($element[0])
        .select('.viz');

    const parts = setup(vizElem);

    const simulation = forceSimulation()
        .force("link", forceLink().id(d => d.id).distance(60))
        .force("charge", forceManyBody().strength(-40));

    vm.$onChanges = () => {
        if (vm.data) {
            // we draw using async to prevent clientWidth reporting '0'
            $scope.$applyAsync(() => draw(vm.data, parts, vm.tweakers, simulation));
        }
    };

    vm.$onDestroy = () => {
        simulation.stop();
    };
}


controller.$inject = ['$scope', '$element'];


const component = {
    bindings,
    template,
    controller
};


export default component;