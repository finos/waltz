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

const dispatcher = new EventDispatcher();

const dimensions = { height: 600, width: 1000 };


function handleNodeClick(node) {
    dispatcher.dispatch({ type: 'NODE_CLICK', data: node });
}


const force = d3.layout.force()
    .gravity(0.05)
    .friction(0.6)
    .distance(100)
    .charge(-100);


function setup(holder) {
    const svg = d3.select(holder)
        .append('svg:svg')
        .attr({ width: dimensions.width, height: dimensions.height });

    force.size([dimensions.width, dimensions.height]);

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

    return { svg };
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


function draw(data, parts, tweakers) {

    const nodes = data.entities;
    const nodeIds = _.map(nodes, 'id');

    const links = _.map(data.flows, f => ({
        source: _.indexOf(nodeIds, f.source.id),
        target: _.indexOf(nodeIds, f.target.id),
        data: f
    }));

    force
        .links(links)
        .nodes(data.entities)
        .start();

    const link = parts.svg.selectAll('.wdfd-link')
        .data(links);

    link.enter()
        .append('svg:line')
        .attr('marker-end', 'url(#arrowhead)')
        .classed('wdfd-link', true);


    link.exit()
        .remove();

    const node = parts.svg.selectAll('.wdfd-node')
        .data(data.entities, n => n.id);

    node.enter()
        .append('g')
        .classed('wdfd-node', true)
        .call(tweakers.node.enter)
        .call(force.drag)
        .call(addNodeCircle)
        .call(addNodeLabel);

    node.exit()
        .remove();


    force.on('tick', () => {

        link.each(function () {this.parentNode.insertBefore(this, this); });

        link.attr('x1', d => d.source.x)
            .attr('y1', d => d.source.y)
            .attr('x2', d => d.target.x)
            .attr('y2', d => d.target.y);

        node.attr('transform', d => { return `translate(${d.x}, ${d.y})`; });
    });
}

function controller($scope) {

    const vm = this;

    dispatcher.subscribe((e) => {
        $scope.$applyAsync(() => vm.focusItem = e.data);
    }, 'NODE_CLICK');

    vm.focusItem = null;

    $scope.$watch('vizElem', (vizElem) => {
        if (vizElem) {
            dimensions.width = vizElem.offsetWidth;
            const parts = setup(vizElem);
            $scope.$watch('ctrl.data', () => {
                if (vm.data && vm.tweakers) {
                    draw(vm.data, parts, vm.tweakers);
                }
            });
        }
    });
}

controller.$inject = ['$scope'];


export default () => ({
    restrict: 'E',
    template: require('./boingy-graph.html'),
    scope: {},
    bindToController: {
        data: '=',
        tweakers: '='
    },
    link: (scope, elem) => {
        const vizElem = elem[0].querySelector('.viz');
        scope.vizElem = vizElem;
    },
    controllerAs: 'ctrl',
    controller
});
