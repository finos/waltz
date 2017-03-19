/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {select, event} from 'd3-selection';
import {drag} from 'd3-drag';
import {initialiseData} from '../../common';
import {mkLineWithArrowPath, responsivefy} from '../../common/d3-utils';
import {d3ContextMenu} from './d3-context-menu';
import {mkModel, toGraphFlow, toGraphNode, toGraphId} from './flow-diagram-utils';
import angular from 'angular';

/**
 * @name waltz-flow-diagram
 *
 * @description
 * This component ...
 */


const bindings = {
    nodes: '<',
    flows: '<',
    layout: '<',
    contextMenus: '<'
};


const initialState = {};


const template = require('./flow-diagram.html');


const styles = {
    NODES: 'wfd-nodes',
    NODE: 'wfd-node',
    FLOWS: 'wfd-flows',
    FLOW: 'wfd-flow',
    TITLE: 'wdf-title'
};


const dimensions = {
    svg: {
        w: 1200,
        h: 900
    },
    node: {
        h: 40,
        w: 90,
        rx: 3,
        ry: 3,
        title: {
            dx: 3,
            dy: 12
        }
    }
};



const state = {
    model: {
        nodes: [],
        flows: []
    },
    layout: {
        // id -> { x, y }
    },
    groups: {
        svg: null,
        nodes: null,
        flows: null
    },
    contextMenus: null
};



function dragStarted(d) {
    select(this)
        .raise()
        .classed("wfd-active", true);
}


function dragged(d) {
    const cmd = {
        command: 'MOVE_NODE',
        payload: { node: d, dx: event.dx, dy: event.dy }
    };
    processCommands([ cmd ]);
}


function dragEnded(d) {
    select(this)
        .classed("wfd-active", false);
}



function layoutFor(d) {
    const dflt = { x: 0, y: 0 };

    const id = _.isString(d)
        ? d
        : d.id;

    const p = state.layout[id];
    if (!p) {
        state.layout[id] = dflt;
    }

    return state.layout[id];
}


const processCommands = (resp = []) => {
    _.forEach(resp, cmd => {
        switch (cmd.command) {
            case 'MOVE_NODE':
                layoutFor(cmd.payload.node).x += cmd.payload.dx;
                layoutFor(cmd.payload.node).y += cmd.payload.dy;
                break;
            case 'ADD_NODE':
                state.model.nodes = _.concat(state.model.nodes, [ toGraphNode(cmd.payload) ]);
                const initialPosition = {
                    x: _.random(100, dimensions.svg.w - 200),
                    y: _.random(100, dimensions.svg.h - 200)
                };
                state.layout[toGraphId(cmd.payload)] = initialPosition;
                break;
            case 'ADD_FLOW':
                state.model.flows = _.concat(state.model.flows, [ toGraphFlow(cmd.payload) ]);
                break;
            default:
                console.log('WFD: unknown command', cmd);
                break;
        }
    });
    draw();
};


function drawNodes(nodes = [], group) {
    if (!group || _.isEmpty(nodes)) return;

    const nodeElems = group
        .selectAll(`.${styles.NODE}`)
        .data(nodes, d => d.id);

    const newNodeElems = nodeElems
        .enter()
        .append('g')
        .classed(styles.NODE, true)
        .on('contextmenu', d3ContextMenu(
            state.contextMenus.node,
            { onClose: processCommands }));

    newNodeElems
        .merge(nodeElems)
        .attr('transform', d => `translate(${layoutFor(d).x}, ${layoutFor(d).y})`)
        .call(drag()
            .on("start", dragStarted)
            .on("drag", dragged)
            .on("end", dragEnded));

    newNodeElems
        .append('rect')
        .attr('width', dimensions.node.w)
        .attr('height', dimensions.node.h)
        .attr('rx', dimensions.node.rx)
        .attr('ry', dimensions.node.ry);


    newNodeElems
        .append('text')
        .text(d => d.data.name)
        .classed(styles.TITLE, true)
        .attr('dx', dimensions.node.title.dx)
        .attr('dy', dimensions.node.title.dy);
}


function drawFlows(flows = [], group) {
    if (!group || _.isEmpty(flows)) return;

    const linkElems = group
        .selectAll(`.${styles.FLOW}`)
        .data(flows, d => d.id);

    const newLinkElems = linkElems
        .enter()
        .append('g')
        .classed(styles.FLOW, true);

    newLinkElems
        .append('path');

    newLinkElems
        .merge(linkElems)
        .selectAll('path')
        .attr('d', d => mkLineWithArrowPath(
            layoutFor(d.source).x + (dimensions.node.w / 2),
            layoutFor(d.source).y + (dimensions.node.h / 2),
            layoutFor(d.target).x + (dimensions.node.w / 2),
            layoutFor(d.target).y + (dimensions.node.h / 2),
            1 /* arrow in center */));
}


const logger = _.throttle(() => console.log('draw', state), 400);

function draw() {
    logger();
    drawFlows(state.model.flows, state.groups.flows);
    drawNodes(state.model.nodes, state.groups.nodes);
}


function prepareGroups(holder) {
    const svg = select(holder)
        .append('svg')
        .attr("width", dimensions.svg.w)
        .attr("height", dimensions.svg.h)
        .attr('viewBox', `0 0 ${dimensions.svg.w} ${dimensions.svg.h}`);

    const flows = svg
        .append('g')
        .classed(styles.FLOWS, true);

    const nodes = svg
        .append('g')
        .classed(styles.NODES, true);

    return {
        svg, flows, nodes
    };
}


function controller($element) {
    const vm = this;
    let destroyResizeListener = null;

    vm.$onInit = () => {
        initialiseData(vm, initialState);
        const holder = $element.find('div')[0];
        state.groups = prepareGroups(holder);
        state.groups
            .svg
            .on('contextmenu', d3ContextMenu(
                state.contextMenus.canvas,
                { onClose: processCommands }));
        destroyResizeListener = responsivefy(state.groups.svg);
        vm.$onChanges();
    };

    vm.$onChanges = (c) => {
        console.log('oC', c)

        state.model = mkModel(vm.nodes, vm.flows);
        state.layout = angular.copy(vm.layout);
        state.contextMenus = vm.contextMenus;
        draw();
    };

    vm.$onDestroy = () => {
        destroyResizeListener();
    };
}


controller.$inject = [
    '$element'
];


const component = {
    template,
    bindings,
    controller
};


export default component;