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
import {toGraphId, toNodeShape, shapeFor, positionFor} from '../../flow-diagram-utils';


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


const groups = {
    svg: null,
    nodes: null,
    flows: null
};


const contextMenus = {
    annotation: null,
    canvas: null,
    flowBucket: null,
    node: null
};


function drawNodeShape(selection, state) {
    return selection
        .attr('d', d => {
            const path = shapeFor(state, d).path
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


function dragger(commandProcessor) {
    return (d) => {
        const cmd = {
            command: 'MOVE',
            payload: {id: d.id, dx: event.dx, dy: event.dy}
        };
        commandProcessor([cmd]);
    };
}


function dragEnded(d) {
    select(this)
        .classed("wfd-active", false);
}


function drawNodes(state, group, commandProcessor) {
    if (!group) return;

    const nodeElems = group
        .selectAll(`.${styles.NODE}`)
        .data(state.model.nodes || [], d => d.id);

    nodeElems
        .exit()
        .remove();

    const newNodeElems = nodeElems
        .enter()
        .append('g')
        .classed(styles.NODE, true)
        .on('contextmenu', d3ContextMenu(
            contextMenus.node))
        .call(drag()
            .on("start", dragStarted)
            .on("drag", dragger(commandProcessor))
            .on("end", dragEnded));

    // position and setup drag and drop

    newNodeElems
        .merge(nodeElems)
        .attr('transform', d => {
            const position = positionFor(state, d);
            return `translate(${position.x}, ${position.y})`;
        });

    // draw shape

    newNodeElems
        .append('path');

    nodeElems
        .merge(newNodeElems)
        .selectAll('path')
        .call(drawNodeShape, state);

    // icon

    newNodeElems
        .append('text')
        .classed(styles.TITLE_ICON, true)
        .attr('font-family', 'FontAwesome')
        .text(d => shapeFor(state, d).icon);

    nodeElems
        .merge(newNodeElems)
        .selectAll(`.${styles.TITLE_ICON}`)
        .attr('dx', d => shapeFor(state, d).title.dx)
        .attr('dy', d => shapeFor(state, d).title.dy);

    // title

    newNodeElems
        .append('text')
        .classed(styles.TITLE, true)
        .attr('dx', d => shapeFor(state, d).title.dx + 14)
        .attr('dy', d => shapeFor(state, d).title.dy);

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


function drawFlows(state, group) {
    if (!group) return;

    const linkElems = group
        .selectAll(`.${styles.FLOW}`)
        .data(state.model.flows || [], d => d.id);

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
            const sourcePos = positionFor(state, d.source);
            const targetPos = positionFor(state, d.target);

            const sourceShape = shapeFor(state, d.source);
            const targetShape = shapeFor(state, d.target);

            return mkLineWithArrowPath(
                sourcePos.x + sourceShape.cx,
                sourcePos.y + sourceShape.cy,
                targetPos.x + targetShape.cx,
                targetPos.y + targetShape.cy,
                1.3 /* arrow in center */);
        });
}


function drawFlowBuckets(state, group) {
    if (! group) return;

    const relevantFlows = _.filter(
        state.model.flows || [],
        f => f.data.kind === 'LOGICAL_DATA_FLOW');

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
            contextMenus.flowBucket));

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
            const sourcePos = positionFor(state, d.source);
            const targetPos = positionFor(state, d.target);

            const sourceShape = shapeFor(state, d.source);
            const targetShape = shapeFor(state, d.target);

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


function drawAnnotations(state, group, commandProcessor) {
    if (!group) return;

    console.log(state.model.annotations, '0000')
    const annotationElems = group
        .selectAll(`.${styles.ANNOTATION}`)
        .data(state.model.annotations || [], d => d.id);

    const newAnnotationElems = annotationElems
        .enter()
        .append('g')
        .classed(styles.ANNOTATION, true)
        .on('contextmenu', d3ContextMenu(
            contextMenus.annotation));

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
            console.log('path', d)
            const subjectPosition = positionFor(state, toGraphId(d.data.entityReference));
            const subjectShape = shapeFor(state, toGraphId(d.data.entityReference));
            const annotationPosition = positionFor(state, d.id);
            const bar = dimensions.annotation.text.w * (annotationPosition.x > 0 ? 1 : -1);
            const sx = subjectPosition.x + subjectShape.cx;
            const sy = subjectPosition.y + subjectShape.cy;
            return `
                M${sx},${sy}
                l${annotationPosition.x},${annotationPosition.y}
                l${bar},0
            `;
        });


    // joint
    newAnnotationElems
        .append('circle')
        .attr('draggy', 10)
        .call(drag()
            .on("start", dragStarted)
            .on("drag", dragger(commandProcessor))
            .on("end", dragEnded));

    annotationElems
        .merge(newAnnotationElems)
        .selectAll('circle')
        .attr('r', dimensions.annotation.circle.r)
        .each(function(d) {
            const subjectPosition = positionFor(state, toGraphId(d.data.entityReference));
            const subjectShape = shapeFor(state, toGraphId(d.data.entityReference));
            const annotationPosition = positionFor(state, d.id);
            const cx = subjectPosition.x + annotationPosition.x + subjectShape.cx;
            const cy = subjectPosition.y + annotationPosition.y + subjectShape.cy;
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
            console.log('ann', d)
            const subjectPosition = positionFor(state, toGraphId(d.data.entityReference));
            const subjectShape = shapeFor(state, toGraphId(d.data.entityReference));
            const annotationPosition = positionFor(state, d.id);
            const bar = annotationPosition.x > 0 ? 10 : dimensions.annotation.text.w * -1;
            const x = subjectPosition.x + annotationPosition.x + bar + subjectShape.cx;
            const y = subjectPosition.y + annotationPosition.y + 18;
            select(this)
                .attr('transform', `translate(${x}, ${y})`);
        })
        .text(d => d.data.note)
        .call(wrapText, dimensions.annotation.text.w - 5);
}


function draw(state, commandProcessor = () => console.log('no command processor given')) {
    console.log('draw', state);

    if (state.layout.diagramTransform) {
        groups
            .container
            .attr("transform", state.layout.diagramTransform);
    }

    drawFlows(state, groups.flows, commandProcessor);
    drawNodes(state, groups.nodes, commandProcessor);
    drawFlowBuckets(state, groups.flows, commandProcessor);
    drawAnnotations(state, groups.annotations, commandProcessor);
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


function setupDragAndZoom(commandProcessor) {
    function zoomed() {
        const t = event.transform;
        commandProcessor([{ command: 'TRANSFORM_DIAGRAM', payload: t }]);
    }

    const myZoom = zoom()
        .scaleExtent([1 / 4, 2])
        .on("zoom", zoomed);

    groups.svg
        .call(myZoom)
        .on('dblclick.zoom', null);
}


function controller($element, flowDiagramStateService) {
    const vm = this;
    let destroyResizeListener = null;

    vm.$onInit = () => {
        initialiseData(vm, initialState);
        const holder = $element.find('div')[0];
        Object.assign(groups, prepareGroups(holder));
        groups
            .svg
            .on('contextmenu', d3ContextMenu(
                contextMenus.canvas));

        destroyResizeListener = responsivefy(groups.svg);

        setupDragAndZoom(flowDiagramStateService.processCommands);

        vm.$onChanges();
    };

    flowDiagramStateService.onChange(() => draw(
        flowDiagramStateService.getState(),
        flowDiagramStateService.processCommands));

    vm.$onChanges = (c) => {
        Object.assign(contextMenus, vm.contextMenus || {});
        if (_.isFunction(vm.onInitialise)) {
            vm.onInitialise({});
        }
        draw(
            flowDiagramStateService.getState(),
            flowDiagramStateService.processCommands);
    };

    vm.$onDestroy = () => {
        destroyResizeListener();
    };
}


controller.$inject = [
    '$element',
    'FlowDiagramStateService'
];


const component = {
    template,
    bindings,
    controller
};



export default component;