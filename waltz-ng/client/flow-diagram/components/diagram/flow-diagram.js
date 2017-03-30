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
import {zoom} from 'd3-zoom';
import {initialiseData} from '../../../common';
import {mkLineWithArrowPath, responsivefy, wrapText} from '../../../common/d3-utils';
import {d3ContextMenu} from '../../../common/d3-context-menu';
import {mkModel, toGraphFlow, toGraphNode, toGraphId, toNodeShape} from '../../flow-diagram-utils';
import angular from 'angular';




/**
 * @name waltz-flow-diagram
 *
 * @description
 * This component ...
 */

const logState = _
    .throttle(
        () => console.log('draw', state),
        400);


const bindings = {
    nodes: '<',
    flows: '<',
    layout: '<',
    contextMenus: '<',
    onInitialise: '<'
};


const initialState = {};


const template = require('./flow-diagram.html');


const styles = {
    ANNOTATION: 'wfd-annotation',
    ANNOTATIONS: 'wfd-annotations',
    FLOW: 'wfd-flow',
    FLOWS: 'wfd-flows',
    FLOW_ARROW: 'wfd-flow-arrow',
    FLOW_BUCKET: 'wfd-flow-bucket',
    NODE: 'wfd-node',
    NODES: 'wfd-nodes',
    SUBJECT: 'wfd-subject',
    TITLE: 'wdf-title',
    TITLE_ICON: 'wdf-title-icon',
};


const dimensions = {
    svg: {
        w: 1000,
        h: 540
    },
    flowBucket: {
        r: 10
    },
    annotation: {
        circle: {
            r: 10
        },
        text: {
            w: 120
        }
    }
};


const state = {
    model: {
        nodes: [],
        flows: [],
        decorations: {},
        annotations: []
    },
    layout: {
        positions: {}, // gid -> {  x, y }
        shapes: {}, // gid -> { path, cx, cy, etc} }
        subject: null
    },
    groups: {
        svg: null,
        nodes: null,
        flows: null
    },
    contextMenus: null
};



function drawNodeShape(selection) {
    return selection
        .attr('d', d => {
            const path = layoutFor(d).shape.path
            return path;
        })
        .attr('stroke', '#ccc')
        .attr('fill', '#eee');
}


function dragStarted(d) {
    select(this)
        .raise()
        .classed("wfd-active", true);
}


function nodeDragged(d) {
    const cmd = {
        command: 'MOVE_NODE',
        payload: { graphNode: d, dx: event.dx, dy: event.dy }
    };
    processCommands([ cmd ]);
}


function annotationDragged(d) {
    const cmd = {
        command: 'MOVE_ANNOTATION',
        payload: { annotation: d, dx: event.dx, dy: event.dy }
    };
    processCommands([ cmd ]);
}


function dragEnded(d) {
    select(this)
        .classed("wfd-active", false);
}


function initialiseShape(graphNode) {
    if (!_.isObject(graphNode)) throw "Cannot initialise shape without an object, was given: " + graphNode;
    const shape = toNodeShape(_.get(graphNode, 'data.kind', 'DEFAULT'));
    state.layout.shapes[graphNode.id] = shape;
    return shape;
}


function initialisePosition(graphNodeId) {
    const position =  { x: 0, y: 0 };
    state.layout.positions[graphNodeId] = position;
    return position;
}


function layoutFor(graphNode) {
    const id = _.isString(graphNode)
        ? graphNode
        : graphNode.id;

    const position = state.layout.positions[id] || initialisePosition(id);
    const shape = state.layout.shapes[id] || initialiseShape(graphNode);

    return {
        position,
        shape
    };
}


function processCommand(command, payload, model) {
    console.log("wFD - processing command: ", command, payload, model);
    switch (command) {
        case 'MOVE_NODE':
            const layout = layoutFor(payload.graphNode);
            layout.position.x += payload.dx;
            layout.position.y += payload.dy;
            break;

        case 'MOVE_ANNOTATION':
            payload.annotation.dx += payload.dx;
            payload.annotation.dy += payload.dy;
            break;

        case 'ADD_ANNOTATION': // { id, note, }
            const existingIds = _.map(model.annotations, "id");
            if (_.includes(existingIds, payload.id)) {
                console.log('Ignoring request to re-add annotation', payload);
            } else {
                model.annotations = _.concat(model.annotations || [], [payload]);
            }
            break;

        case 'UPDATE_ANNOTATION':
            const newText = payload.text;
            const annotation = payload.annotation;
            annotation.note = newText;
            break;

        case 'ADD_NODE':
            const graphNode = toGraphNode(payload);
            const existingIds = _.map(model.nodes, "id");
            if (_.includes(existingIds, graphNode.id)) {
                console.log('Ignoring request to re-add node', payload);
            } else {
                model.nodes = _.concat(model.nodes || [], [ graphNode ]);
                const moveCmd = {
                    graphNode,
                    dx: _.random(100, dimensions.svg.w - 200),
                    dy: _.random(100, dimensions.svg.h - 200)
                };
                processCommands([
                    { command: 'MOVE_NODE', payload: moveCmd}
                ]);
            }
            break;

        case 'ADD_FLOW':
            const graphFlow = toGraphFlow(payload);
            const existingIds = _.map(model.flows, "id");
            if (_.includes(existingIds, graphFlow.id)) {
                console.log('Ignoring request to add duplicate flow', payload);
            } else {
                model.flows = _.concat(model.flows || [], [graphFlow]);
            }
            break;

        case 'ADD_DECORATION':
            const payload = payload;
            const refId = toGraphId(payload.ref);
            const decorationNode = toGraphNode(payload.decoration);
            const currentDecorations = model.decorations[refId] || [];
            const existingIds = _.map(model.flows, "id");
            if (_.includes(existingIds, decorationNode.id)) {
                console.log('Ignoring request to add duplicate decoration');
            } else {
                model.decorations[refId] = _.concat(currentDecorations, [decorationNode]);
            }
            break;

        case 'REMOVE_NODE':
            const flowIdsToRemove = _.chain(model.flows)
                .filter(f => f.source === payload.id || f.target === payload.id)
                .map('id')
                .value();
            model.flows = _.reject(model.flows, f => _.includes(flowIdsToRemove, f.id));
            model.nodes = _.reject(model.nodes, n => n.id === payload.id);
            model.annotations = _.reject(model.annotations, a => toGraphId(a.ref) === payload.id);
            _.forEach(flowIdsToRemove, id => model.decorations[id] = []);
            break;

        case 'REMOVE_ANNOTATION':
            console.log('ra1', model.annotations.length)
            model.annotations = _.reject(model.annotations, a => a.id === payload.id );
            console.log('ra2', model.annotations.length)
            break;
        default:
            console.log('WFD: unknown command', command);
            break;
    }
}


function processCommands(commands = []) {
    _.forEach(commands, cmd => processCommand(cmd.command, cmd.payload, state.model));
    draw();
}


function drawNodes(nodes = [], group) {
    if (!group) return;

    const nodeElems = group
        .selectAll(`.${styles.NODE}`)
        .data(nodes, d => d.id);

    nodeElems
        .exit()
        .remove();

    const newNodeElems = nodeElems
        .enter()
        .append('g')
        .classed(styles.NODE, true)
        .on('contextmenu', d3ContextMenu(
            state.contextMenus.node,
            { onClose: processCommands }))
        .call(drag()
            .on("start", dragStarted)
            .on("drag", nodeDragged)
            .on("end", dragEnded));

    // position and setup drag and drop

    newNodeElems
        .merge(nodeElems)
        .classed(styles.SUBJECT, d => d.id === state.layout.subject)
        .attr('transform', d => {
            const position = layoutFor(d).position;
            return `translate(${position.x}, ${position.y})`;
        });


    // draw shape

    newNodeElems
        .append('path');

    nodeElems
        .merge(newNodeElems)
        .selectAll('path')
        .call(drawNodeShape);

    // icon

    newNodeElems
        .append('text')
        .classed(styles.TITLE_ICON, true)
        .attr('font-family', 'FontAwesome')
        .text(d => layoutFor(d).shape.icon);

    nodeElems
        .merge(newNodeElems)
        .selectAll(`.${styles.TITLE_ICON}`)
        .attr('dx', d => layoutFor(d).shape.title.dx)
        .attr('dy', d => layoutFor(d).shape.title.dy);

    // title

    newNodeElems
        .append('text')
        .classed(styles.TITLE, true)
        .attr('dx', d => layoutFor(d).shape.title.dx + 14)
        .attr('dy', d => layoutFor(d).shape.title.dy);

    nodeElems
        .merge(newNodeElems)
        .selectAll(`.${styles.TITLE}`)
        .text(d => d.data.name)
        .each(function(d) {
            // update shape to accomodate label
            const labelWidth = Math.max(this.getComputedTextLength() + 32, 60);
            state.layout.shapes[d.id] = toNodeShape(d.data, labelWidth);
        });
}


function drawFlows(flows = [], group) {
    if (!group) return;

    const linkElems = group
        .selectAll(`.${styles.FLOW}`)
        .data(flows, d => d.id);

    linkElems
        .exit()
        .remove();

    const newLinkElems = linkElems
        .enter()
        .append('g')
        .classed(styles.FLOW, true);

    newLinkElems
        .append('path')
        .classed(styles.FLOW_ARROW, true);

    newLinkElems
        .merge(linkElems)
        .selectAll(`.${styles.FLOW_ARROW}`)
        .attr('d', d => {
            const sourceLayout = layoutFor(d.source);
            const targetLayout = layoutFor(d.target);

            const sourcePos = sourceLayout.position;
            const targetPos = targetLayout.position;

            const sourceShape = sourceLayout.shape;
            const targetShape = targetLayout.shape;

            return mkLineWithArrowPath(
                sourcePos.x + sourceShape.cx,
                sourcePos.y + sourceShape.cy,
                targetPos.x + targetShape.cx,
                targetPos.y + targetShape.cy,
                1.3 /* arrow in center */);
        });
}


function drawFlowBuckets(flows = [], group) {
    if (! group) return;

    const relevantFlows = _.filter(
        flows,
        f => f.data.kind === 'LOGICAL_FLOW');

    const bucketElems = group
        .selectAll(`.${styles.FLOW_BUCKET}`)
        .data(relevantFlows, f => f.id);

    bucketElems
        .exit()
        .remove();

    const newBucketElems = bucketElems
        .enter()
        .append('g')
        .classed(styles.FLOW_BUCKET, true)
        .on('contextmenu', d3ContextMenu(
            state.contextMenus.flowBucket,
            { onClose: processCommands }));

    newBucketElems
        .append('circle');

    newBucketElems
        .append('text')
        .classed('fa-fw', true)
        .attr('font-family', 'FontAwesome')
        .attr('dx', -6)
        .attr('dy', 5)
        .text(d => {
            const decorationCount = _.size(state.model.decorations[d.id]);
            if (decorationCount === 0) {
                return "\uf29c"; // question
            } else if (decorationCount === 1) {
                return "\uf016"; // one
            } else {
                return "\uf0c5"; // many
            }
        });

    newBucketElems
        .merge(bucketElems)
        .attr('transform', d => {
            // position buckets near center of line
            const sourceLayout = layoutFor(d.source);
            const targetLayout = layoutFor(d.target);

            const sourcePos = sourceLayout.position;
            const targetPos = targetLayout.position;

            const sourceShape = sourceLayout.shape;
            const targetShape = targetLayout.shape;

            const sx = sourcePos.x + sourceShape.cx;
            const sy = sourcePos.y + sourceShape.cy;
            const tx = targetPos.x + targetShape.cx;
            const ty = targetPos.y + targetShape.cy;

            const dx = tx - sx;
            const dy = ty - sy;

            const cx = sx + (dx / 1.8);
            const cy = sy + (dy / 1.8);

            return `translate(${cx},${cy})`;
        })
        .selectAll('circle')
        .attr('r', d => _.size(state.model.decorations[d.id]) > 0
            ? dimensions.flowBucket.r * 1.5
            : dimensions.flowBucket.r );
}


function drawAnnotations(annotations = [], group) {
    if (!group) return;

    const annotationElems = group
        .selectAll(`.${styles.ANNOTATION}`)
        .data(annotations, d => d.id);

    const newAnnotationElems = annotationElems
        .enter()
        .append('g')
        .classed(styles.ANNOTATION, true)
        .on('contextmenu', d3ContextMenu(
            state.contextMenus.annotation,
            { onClose: processCommands }));

    annotationElems
        .exit()
        .remove();

    // line
    newAnnotationElems
        .append('path');

    annotationElems
        .merge(newAnnotationElems)
        .selectAll('path')
        .attr('d', d => {
            const subject = layoutFor(toGraphId(d.ref));
            const bar = dimensions.annotation.text.w * (d.dx > 0 ? 1 : -1);
            const sx = subject.position.x + subject.shape.cx;
            const sy = subject.position.y + subject.shape.cy;
            return `
                M${sx},${sy}
                l${d.dx},${d.dy}
                l${bar},0
            `;
        });


    // joint
    newAnnotationElems
        .append('circle')
        .attr('draggy', 10)
        .call(drag()
            .on("start", dragStarted)
            .on("drag", annotationDragged)
            .on("end", dragEnded));

    annotationElems
        .merge(newAnnotationElems)
        .selectAll('circle')
        .attr('r', dimensions.annotation.circle.r)
        .each(function(d) {
            const subject = layoutFor(toGraphId(d.ref));
            const cx = subject.position.x + d.dx + subject.shape.cx;
            const cy = subject.position.y + d.dy + subject.shape.cy;
            select(this)
                .attr('cx', cx)
                .attr('cy', cy);
        });

    // text
    newAnnotationElems
        .append('text');

    annotationElems
        .merge(newAnnotationElems)
        .selectAll('text')
        .each(function(d) {
            const subject = layoutFor(toGraphId(d.ref));
            const bar = d.dx > 0 ? 10 : dimensions.annotation.text.w * -1;
            const x = subject.position.x + d.dx + bar + subject.shape.cx;
            const y = subject.position.y + d.dy + 18;
            select(this)
                .attr('transform', `translate(${x}, ${y})`);
        })
        .text(d => d.note)
        .call(wrapText, dimensions.annotation.text.w - 5);
}


function draw() {
    logState();
    drawFlows(state.model.flows, state.groups.flows);
    drawNodes(state.model.nodes, state.groups.nodes);
    drawFlowBuckets(state.model.flows, state.groups.flows);
    drawAnnotations(state.model.annotations, state.groups.annotations);
}


function prepareGroups(holder) {
    const svg = select(holder)
        .append('svg')
        .attr("width", dimensions.svg.w)
        .attr("height", dimensions.svg.h)
        .attr('viewBox', `0 0 ${dimensions.svg.w} ${dimensions.svg.h}`);

    const container = svg
        .append('g');

    const annotations = container
        .append("g")
        .classed(styles.ANNOTATIONS, true);

    const flows = container
        .append('g')
        .classed(styles.FLOWS, true);

    const nodes = container
        .append('g')
        .classed(styles.NODES, true);

    return {
        svg,
        container,
        annotations,
        flows,
        nodes
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

        function zoomed() {
            const t = event.transform;
            state.groups
                .container
                .attr("transform", t);
        }

        const myZoom = zoom()
            .scaleExtent([1 / 4, 2])
            .on("zoom", zoomed);

        state.groups
            .svg
            .call(myZoom)
            .on('dblclick.zoom', null);

        vm.$onChanges();
    };

    vm.$onChanges = (c) => {
        state.model = mkModel(vm.nodes, vm.flows);
        state.layout = angular.copy(vm.layout);
        state.contextMenus = vm.contextMenus;
        if (_.isFunction(vm.onInitialise)) {
            vm.onInitialise({ processCommands });
        }
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