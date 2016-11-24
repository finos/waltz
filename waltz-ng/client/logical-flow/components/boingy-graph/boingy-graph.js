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

import {select} from 'd3-selection';
import {forceSimulation, forceLink, forceManyBody, forceCenter} from 'd3-force';
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


function setup(vizElem) {
    const svg = vizElem
        .append('svg');

    return { svg, vizElem };
}


function addNodeCircle(selection) {
    selection
        .append('circle')
        .attrs({
            cx: 0,
            cy: 0,
            r: 8
        });
}


function addNodeLabel(selection) {
    selection
        .append('text')
        .attrs({ dx: 10, dy: '.35em' })
        .text(d => d.name);
}


function drawLinks(flows = [], nodes = [], svg, linkTweakers = DEFAULT_TWEAKER) {
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


function drawNodes(entities = [], svg, nodeTweakers = DEFAULT_TWEAKER) {
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


function mkLinks(flows = [], nodes = []) {
    return _.map(
        flows,
        f => ({
            source: f.source.id,
            target: f.target.id,
            data: f
        }));
}


function draw(data, parts, tweakers = { node: DEFAULT_TWEAKER, link: DEFAULT_TWEAKER }) {
    const dimensions = setupDimensions(parts.vizElem);
    parts.svg.attrs({
        width: dimensions.width,
        height: dimensions.height
    });

    const links = mkLinks(data.flows, data.entities)
    console.log(links);

    const simulation = forceSimulation()
        .force("link", forceLink().id(d => d.id))
        .force("charge", forceManyBody())
        .force("center", forceCenter(dimensions.width / 2, dimensions.height / 2));

    const link = parts
        .svg
        .append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(links)
        .enter()
        .append("line")
        .attr("stroke-width", 2);


    const node = parts
        .svg
        .append("g")
        .attr("class", "nodes")
        .selectAll("circle")
        .data(graph.nodes)
        .enter().append("circle")
        .attr("r", 5)
        .attr("fill", 'red')
        .call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended));
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
            $scope.$applyAsync(() => draw(vm.data, parts, vm.tweakers));
        }
    };
}


controller.$inject = ['$scope', '$element'];


const component = {
    bindings,
    template,
    controller
};


export default component;