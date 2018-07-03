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
import _ from 'lodash';
import {event} from 'd3-selection';
import {drag} from 'd3-drag';
import {forceSimulation, forceCollide, forceManyBody, forceLink, forceX, forceY} from 'd3-force';

import template from './flow-explorer.html';
import {initialiseData} from "../../../common/index";
import {lineWithArrowPath} from '../../../common/d3-utils';
import {CORE_API} from '../../../common/services/core-api-utils';
import {
    addNodeLabel, addNodeSymbol, calcDownstreams, concatFlows, concatNodesFromFlows, flowToGraphId,
    mkClassSelector,
    nodeToGraphId,
    prepareGroups,
    Styles
} from "./flow-explorer-utils";


// -- d3

const dimensions = {
    svg: {
        w: 1100,
        h: 600
    }
};


const simulation = forceSimulation()
    .force('charge', forceManyBody()
        .strength(-20))
    .force('collide', forceCollide(15))
    .force('x', forceX(dimensions.svg.w / 2)
        .strength(0.4)
        .x(n => {
            const centerPoint = dimensions.svg.w / 4 * 3;  // 'center is 3/4 to the right'

            const downstreams = n.downstreams || [];
            const minXs = _.flatMap(downstreams, d => {
                return d.fx
                    ? [d.fx, d.x]
                    : [d.x];
            });

            const minX = _.min(minXs);
            return (minX || centerPoint) - 100;
        }))
    .force('y', forceY(dimensions.svg.h / 2))
    .force('link', forceLink()
        .id(f => f.id)
        .distance(150))
;


function drawFlows(holder, flows = []) {
    const flowSelection = holder
        .selectAll(mkClassSelector(Styles.FLOW))
        .data(flows, f => f.id);

    const newFlows = flowSelection
        .enter()
        .append('path')
        .classed(Styles.FLOW, true);

    flowSelection
        .exit()
        .remove();

    return newFlows
        .merge(flowSelection);
}


function dragStarted(d) {
    if (! event.active) {
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
    if (! event.active) {
        simulation
            .alphaTarget(0.0)
            .restart();
    }
    // delete d.fx;
    // delete d.fy;
}


function drawNodes(holder, nodes = [], handlers = {}) {
    const nodeSelection = holder
        .selectAll(mkClassSelector(Styles.NODE))
        .data(nodes, n => n.id);

    const newNodes = nodeSelection
        .enter()
        .append('g')
        .classed(Styles.NODE, true);

    newNodes
        .on('dblclick', handlers.dblclick)
        .call(drag()
            .on('start', dragStarted)
            .on('drag', dragged)
            .on('end', dragEnded))
        .call(addNodeSymbol)
        .call(addNodeLabel);

    nodeSelection
        .exit()
        .remove();

    return nodeSelection
        .merge(newNodes);
}


function draw(groups, graphData, handlers) {
    const linkSelection = drawFlows(groups.flows, graphData.flows);
    const nodeSelection = drawNodes(groups.nodes, graphData.nodes, handlers.node);

    const ticked = () => {
        nodeSelection
            .attr('transform', d => `translate(${d.x}, ${d.y})`);

        linkSelection
            .call(lineWithArrowPath, 0.3)
    };

    simulation
        .nodes(graphData.nodes)
        .on('tick', ticked);

    simulation
        .force('link')
        .links(graphData.flows)
}


// -- angular

const initialState = {

};


const bindings = {
    parentEntityRef: '<'
};


function controller($element, $timeout, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const graphData = {
        nodes: [],
        flows: [],
        filters: [],
        positionHints: {}
    };

    const groups = {
        svg: null,
        nodes: null,
        flows: null,
        container: null
    };

    function updateGraphModel(flowData, centerId) {
        graphData.nodes = concatNodesFromFlows(graphData.nodes, flowData);
        graphData.flows = concatFlows(graphData.flows, flowData);
        graphData.downstreams = calcDownstreams(graphData, _.map(flowData, flowToGraphId), centerId);

        graphData.nodes = _.map(graphData.nodes, n => {
            return Object.assign(
                {},
                n,
                { downstreams: graphData.downstreams[n.id] });
        });
    }

    const handlers = {
        node: {
            dblclick: d => loadFlows(d.waltzData, true)
        }
    };

    function loadFlows(entityRef, incomingOnly) {
        const centerId = nodeToGraphId(vm.parentEntityRef);
        return serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [entityRef])
            .then(r => {
                const flows = r.data;
                return incomingOnly
                    ? _.filter(flows, f => f.target.kind === entityRef.kind && f.target.id === entityRef.id)
                    : flows
            })
            .then(flows => updateGraphModel(flows, centerId))
            .then(kickSimulation);
    }

    function kickSimulation() {
        simulation.alphaTarget(0.01);
        simulation.restart();
        draw(groups, graphData, handlers);
    }

    vm.$onInit = () => {
        const holder = $element.find('div')[0];
        Object.assign(groups, prepareGroups(holder, dimensions.svg));

        loadFlows(vm.parentEntityRef, false)
            .then(() => loadFlows({ kind: 'APPLICATION', id: 137}, true))
    };

}


controller.$inject = [
    '$element',
    '$timeout',
    'ServiceBroker'
];


export const component = {
    bindings,
    controller,
    template
};


export const id = 'waltzFlowExplorer';