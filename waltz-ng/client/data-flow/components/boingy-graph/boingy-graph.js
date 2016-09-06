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

import d3 from "d3";
import _ from "lodash";
import EventDispatcher from "../../../common/EventDispatcher";


const template = require('./boingy-graph.html');

const bindings = {
    data: '<',
    tweakers: '<'
};


const dispatcher = new EventDispatcher();

function setupDimensions(vizElem) {
    const width = vizElem.node().clientWidth || 1000;
    return { width, height: 600 };
}


function handleNodeClick(node) {
    dispatcher.dispatch({ type: 'NODE_CLICK', data: node });
}


const force = d3.layout.force()
    .distance(100)
    .linkDistance(60)
    .charge(-120);


function setup(vizElem) {
    const svg = vizElem
        .append('svg');


    svg.append('defs').append('marker')
        .attr({
            id: 'arrowhead',
            refX: 20,
            refY: 4,
            markerWidth: 8,
            markerHeight: 8,
            orient: 'auto'
        })
        .append('path')
        .attr('d', 'M 0,0 V 8 L8,4 Z'); // this is actual shape for arrowhead

    return { svg, vizElem };
}


function addNodeCircle(selection) {
    selection
        .append('circle')
        .attr({
            cx: 0,
            cy: 0,
            r: 8
        })
        .on('click', handleNodeClick);
}


function addNodeLabel(selection) {
    selection
        .append('text')
        .attr({ dx: 10, dy: '.35em' })
        .text(d => d.name)
        .on('click', handleNodeClick);
}


function drawLinks(flows = [], nodes = [], svg) {
    const nodeIds = _.map(nodes, 'id');

    const links = _.map(
        flows,
        f => ({
            source: _.indexOf(nodeIds, f.source.id),
            target: _.indexOf(nodeIds, f.target.id),
            data: f
        }));

    force
        .links(links)
        .nodes(nodes)
        .start();

    const link = svg
        .selectAll('.wdfd-link')
        .data(links);

    link.enter()
        .append('svg:line')
        .attr('marker-end', 'url(#arrowhead)')
        .classed('wdfd-link', true);

    link.exit()
        .remove();

    return link;
}


function drawNodes(entities = [], svg, nodeTweakers) {
    const node = svg
        .selectAll('.wdfd-node')
        .data(entities, n => n.id);

    node.enter()
        .append('g')
        .classed('wdfd-node', true)
        .call(nodeTweakers.enter)
        .call(force.drag)
        .call(addNodeCircle)
        .call(addNodeLabel);

    node.exit()
        .remove();

    return node;
}


function draw(data, parts, tweakers) {

    const dimensions = setupDimensions(parts.vizElem);

    force.size([dimensions.width, dimensions.height]);

    parts.svg.attr({ width: dimensions.width, height: dimensions.height });

    const linkSelection = drawLinks(data.flows, data.entities, parts.svg);

    const nodeSelection = drawNodes(data.entities, parts.svg, tweakers.node);

    force.on('tick', () => {
        linkSelection
            .each(function () {this.parentNode.insertBefore(this, this); });

        linkSelection
            .attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        nodeSelection
            .attr('transform', d => { return `translate(${d.x}, ${d.y})`; });
    });
}


function controller($scope, $element) {
    const vm = this;

    const vizElem = d3
        .select($element[0])
        .select('.viz');

    const parts = setup(vizElem);

    dispatcher.subscribe((e) => {
        $scope.$applyAsync(() => vm.focusItem = e.data);
    }, 'NODE_CLICK');

    vm.focusItem = null;

    vm.$onChanges = () => {
        if (vm.data && vm.tweakers) {
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