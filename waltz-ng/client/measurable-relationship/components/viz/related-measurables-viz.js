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
import {initialiseData} from '../../../common';
import {responsivefy} from '../../../common/d3-utils';
import {stopPropagation} from '../../../common/browser-utils';
import {select, event} from 'd3-selection';


/**
 * @name waltz-related-measurables-viz
 *
 * @description
 * This component ...
 */


const bindings = {
    categories: '<',
    measurables: '<',
    measurable: '<',
    relatedByCategory: '<',
    onCategorySelect: '<',
    onCategoryClear: '<'
};


const initialState = {
    categories: [],
    measurables: [],
    measurable: null,
    relatedByCategory: {},
    onCategorySelect: (c) => console.log('wrmv: default on-category-select', c),
    onCategoryClear: (c) => console.log('wrmv: default on-category-clear')
};


const template = require('./related-measurables-viz.html');


const dimensions = {
    width: 460,
    height: 460,
    centerNode: {
        r: 75
    },
    outerNode: {
        distanceFromCenter: 170,
        r: 45
    },
    bridge: {
        w: 2
    },
    nodeName: {
        dy: 1
    },
    nodeDescription: {
        dy: 16
    }
};


const angleOffset = 0.3;


const styles = {
    centerNodes: 'wrmv-center-nodes',
    centerNode: 'wrmv-center-node',
    nodeName: 'name',
    nodeDetail: 'detail',
    outerNodes: 'wrmv-outer-nodes',
    outerNode: 'wrmv-outer-node',
    bridges: 'wrmv-bridges',
    bridge: 'wrmv-bridge',
    hasRelationships: 'has-relationships',
    noRelationships: 'no-relationships'
};


function prepareGroups(holder, onCategoryClear) {
    const svg = select(holder)
        .attr('width', dimensions.width)
        .attr('height', dimensions.height)
        .on('click', () => onCategoryClear());

    const bridges = svg.append('g').classed(styles.bridges, true);
    const centerNodes = svg.append('g').classed(styles.centerNodes, true);
    const outerNodes = svg.append('g').classed(styles.outerNodes, true);

    return {
        svg,
        centerNodes,
        bridges,
        outerNodes
    };
}


function calculatePositionOfOuterNode(angle) {
    const r = dimensions.outerNode.distanceFromCenter;
    const x = r * Math.cos(angle) + dimensions.width / 2;
    const y = r * Math.sin(angle) + dimensions.height / 2;
    return { x, y };
}


function drawCenterGroup(group, measurable, category) {
    const centerGroup = group
        .selectAll(`.${styles.centerNode}`)
        .data([measurable], d => d.id)
        .enter()
        .append('g')
        .classed(styles.centerNode, true);

    centerGroup
        .append('circle')
        .attr('cx', dimensions.width / 2)
        .attr('cy', dimensions.height / 2)
        .attr('r', dimensions.centerNode.r)
        .attr('fill', 'white')
        .attr('stroke', 'red');

    centerGroup
        .append('text')
        .text((d, i) => d.name)
        .classed(styles.nodeName, true)
        .attr('text-anchor', 'middle')
        .attr('x', dimensions.width / 2)
        .attr('y', dimensions.height / 2)
        .attr('dy', dimensions.nodeName.dy);

    centerGroup
        .append('text')
        .text((d, i) => category.name)
        .classed(styles.nodeDetail, true)
        .attr('text-anchor', 'middle')
        .attr('x', dimensions.width / 2)
        .attr('y', dimensions.height / 2)
        .attr('dy', dimensions.nodeDescription.dy);
}


function drawOuterNodes(group, categories = [], relatedByCategory = {}, deltaAngle, handlers) {
    const outerNodes = group
        .selectAll(`.${styles.outerNode}`)
        .data(categories, d => d.id);

    const newOuterNodes = outerNodes
        .enter()
        .append('g')
        .classed(styles.outerNode, true)
        .attr('transform', (d, i) => {
            const { x, y } = calculatePositionOfOuterNode(deltaAngle(i));
            return `translate(${x}, ${y})`;
        })
        .on('click', d => {
            handlers.onCategorySelect(d);
            stopPropagation(event);
        });

    newOuterNodes
        .append('circle')
        .attr('stroke', '#ccc')
        .attr('fill', 'white');

    newOuterNodes
        .append('text')
        .classed(styles.nodeName, true)
        .text(d => d.name)
        .attr('text-anchor', 'middle')
        .attr('dy', dimensions.nodeName.dy);

    newOuterNodes
        .append('text')
        .classed(styles.nodeDetail, true);

    outerNodes
        .merge(newOuterNodes)
        .classed(styles.hasRelationships, d => (relatedByCategory[d.id] || []).length > 0)
        .classed(styles.noRelationships, d => (relatedByCategory[d.id] || []).length === 0);

    outerNodes
        .merge(newOuterNodes)
        .selectAll(`circle`)
        .attr('r', d => {
            const hasRelationships = (relatedByCategory[d.id] || []).length > 0;
            const scaleFactor = hasRelationships
                ? 1
                : 0.8;
            return dimensions.outerNode.r * scaleFactor;
        });

    outerNodes
        .merge(newOuterNodes)
        .selectAll(`.${styles.nodeDetail}`)
        .text(d => {
            const count = (relatedByCategory[d.id] || []).length;
            return count ? count : '-'
        })
        .attr('text-anchor', 'middle')
        .attr('dy', dimensions.nodeDescription.dy);

    outerNodes
        .exit()
        .remove();
}


function drawBridges(group, categories = [], deltaAngle) {
    return group
        .selectAll(`.${styles.bridge}`)
        .data(categories, d => d.id)
        .enter()
        .append('line')
        .classed(styles.bridge, true)
        .attr('x1', dimensions.width / 2)
        .attr('y1', dimensions.height / 2)
        .attr('stroke', '#aaa')
        .attr('stroke-width', dimensions.bridge.w)
        .each(function (d, i) {
            const { x, y } = calculatePositionOfOuterNode(deltaAngle(i));
            select(this)
                .attr('x2', x)
                .attr('y2', y);
        });
}


function draw(groups, data, handlers) {
    if (! groups) return;
    if (! data.category) return;
    if (! data.categories) return;

    console.log('draw', groups, data);

    const categoriesById = _.keyBy(data.categories, 'id');
    const deltaAngle = i => i * (Math.PI * 2) / data.categories.length + angleOffset;

    drawCenterGroup(groups.centerNodes, data.measurable, categoriesById[data.measurable.categoryId]);
    drawOuterNodes(groups.outerNodes, data.categories, data.relatedByCategory, deltaAngle, handlers);
    drawBridges(groups.bridges, data.categories, deltaAngle);
}


function mkHandlers(vm) {
    return {
        onCategoryClear: vm.onCategoryClear,
        onCategorySelect: vm.onCategorySelect
    };
}


function controller($element) {
    const vm = this;

    let destroyResizeListener = () => {
    };
    let groups = {};
    let data = {};

    vm.$onInit = () => {
        initialiseData(vm, initialState);
        const holder = $element.find('svg')[0];
        groups = prepareGroups(holder, vm.onCategoryClear);

        destroyResizeListener = responsivefy(groups.svg);

        draw(groups, data, mkHandlers(vm));
    };

    vm.$onChanges = (c) => {
        data.categories = vm.categories || [];
        data.measurables = vm.measurables || [];
        data.relatedByCategory = vm.relatedByCategory || {};
        if (vm.measurable) {
            data.measurable = vm.measurable;
            data.category = _.find(vm.categories || [], {id: vm.measurable.categoryId});
        }
        draw(groups, data, mkHandlers(vm));
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