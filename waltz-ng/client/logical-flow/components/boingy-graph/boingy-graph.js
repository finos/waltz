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


function setupDimensions(vizElem) {
    const width = vizElem.node().clientWidth || 1000;
    return { width, height: 600 };
}


const force = forceSimulation()
// TODO: d3-v4
    // .distance(100)
    // .linkDistance(70)
    // .gravity(0.05)
    // .charge(-200);



function drawLinksOld(flows = [], nodes = [], svg, linkTweakers = DEFAULT_TWEAKER) {
    const nodeIds = _.map(nodes, 'id');

    const links = _.map(
        flows,
        f => ({
            source: _.indexOf(nodeIds, f.source.id),
            target: _.indexOf(nodeIds, f.target.id),
            data: f
        }));

    if (links.length > 0) {
        force
            .links(links)
            .nodes(nodes)
            .start();
    }

    const link = svg
        .selectAll('.wdfd-link')
        .data(links, f => f.data.id);

    link.enter()
        .append('svg:line')
        .attr('marker-end', 'url(#arrowhead)') // defined in common-svg-defs
        .attr('stroke', '#333')
        .classed('wdfd-link', true)
        .call(linkTweakers.enter);

    link
        .call(linkTweakers.update);

    link.exit()
        .call(linkTweakers.exit)
        .remove();

    return link;
}


function drawNodesOld(entities = [], svg, nodeTweakers = DEFAULT_TWEAKER) {
    const node = svg
        .selectAll('.wdfd-node')
        .data(entities, n => n.id);

    node.enter()
        .append('g')
        .classed('wdfd-node', true)
        .on('click.fix', d => d.fixed = true)
        .on('dblclick.unfix', d => d.fixed = false)
        .call(force.drag)
        .call(addNodeCircle)
        .call(addNodeLabel)
        .call(nodeTweakers.enter);

    node.exit()
        .remove();

    node.call(nodeTweakers.update);

    return node;
}

function animateLinks(linkSelection, nodeSelection, linkTweakers = DEFAULT_TWEAKER) {

    force.on('tick', () => {
        linkSelection
            .each(function () {this.parentNode.insertBefore(this, this); });

        linkSelection
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y)
            .call(linkTweakers.update);

        nodeSelection
            .attrs('transform', d => { return `translate(${d.x}, ${d.y})`; });
    });
}


// --- NEW v /  OLD ^ ---

function drawLinks(links = [], holder) {
    return holder
        .selectAll("links")
        .data(links)
        .enter()
        .append("line")
        .classed('wdfd-link', true)
        //.attr('marker-end', 'url(#arrowhead)') // defined in common-svg-defs
        .attr('stroke', '#333')
        // .call(linkTweakers.enter);
        .attr('stroke', 'pink')
        .attr('stroke-width', 4);
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
        .selectAll("g.node")
        .data(nodes);

    const newNodes = nodeSelection
        .enter()
        .append('g')
        .classed('wdfd-node', true)
        .on('dblclick.unfix', d => {
            console.log('unfix')
            d.fx = null;
            d.fy = null;
        })
        .call(tweakers.enter)
        .call(drag()
            .on("start", dragStarted)
            .on("drag", dragged)
            .on("end", dragEnded));

    newNodes
        .call(addNodeLabel);

    newNodes
        .call(addNodeCircle);


    nodeSelection
        .exit()
        .remove();

    return nodeSelection.merge(newNodes);
}


function setup(vizElem) {
    const svg = vizElem
        .append('svg');

    const nG = svg.append("g")
        .attr("class", "nodes");

    const lG = svg.append("g")
        .attr("class", "links");

    const myZoom = zoom()
        .scaleExtent([1 / 4, 2])
        .on("zoom", zoomed);

    svg.call(myZoom)
        .on("dblclick.zoom", null);

    function zoomed() {
        const t = event.transform;
        nG.attr("transform", t);
        lG.attr("transform", t);
    }

    return { svg, vizElem };
}



function mkLinks(flows = [], nodes = []) {
    return _.map(
        flows,
        f => ({
            source: f.source.id,
            target: f.target.id,
            data: f
        }));
}


function draw(data,
              parts,
              tweakers = { node: DEFAULT_TWEAKER, link: DEFAULT_TWEAKER },
              simulation) {

    const links = mkLinks(data.flows, data.entities);

    const dimensions = setupDimensions(parts.vizElem);
    parts.svg.attrs({
        width: dimensions.width,
        height: dimensions.height
    });

    simulation
        .force("center", forceCenter(dimensions.width / 2, dimensions.height / 2));

    const link = drawLinks(
        links,
        parts.svg.select('g.links'));

    const node = drawNodes(
        data.entities,
        parts.svg.select('g.nodes'),
        simulation,
        tweakers.node);

    simulation
        .nodes(data.entities)
        .on("tick", ticked);

    simulation.force("link")
        .links(links);

    function ticked() {
        link
            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; })
            .call(tweakers.link.update);

        node
            .attr('transform', d => `translate(${d.x}, ${d.y})`);
    }
    //
    //
    // const linkSelection = drawLinks(data.flows, data.entities, parts.svg, tweakers.link);
    // const nodeSelection = drawNodes(data.entities, parts.svg, tweakers.node);
    // animateLinks(linkSelection, nodeSelection, tweakers.link);
}


function controller($scope, $element) {
    const vm = this;

    const vizElem = select($element[0])
        .select('.viz');

    const parts = setup(vizElem);

    vm.$onChanges = () => {
        if (vm.data) {
            // we draw using async to prevent clientWidth reporting '0'
            $scope.$applyAsync(() => draw(vm.data, parts, vm.tweakers, simulation));
        }
    };


    const simulation = forceSimulation()
        .force("link", forceLink().id(d => d.id).distance(60))
        .force("charge", forceManyBody().strength(-40));

    vm.$onDestroy = () => {
        simulation.stop();
    }
}


controller.$inject = ['$scope', '$element'];


const component = {
    bindings,
    template,
    controller
};


export default component;