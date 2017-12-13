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
import {select} from 'd3-selection';
import {symbol, symbolWye} from 'd3-shape';



// -- svg

export const Styles = {
    FLOW: 'wfe-flow',
    FLOWS: 'wfe-flows',
    NODE: 'wfe-node',
    NODES: 'wfe-nodes',
    GLYPH: 'wfe-glyph'
};


export function prepareGroups(holder, dimensions) {
    const svg = select(holder)
        .append('svg')
        .attr("width", dimensions.w)
        .attr("height", dimensions.h)
        .attr('viewBox', `0 0 ${dimensions.w} ${dimensions.h}`);

    const container = svg
        .append('g')
        .attr('transform', 'translate(0,0) scale(1)');

    const flows = container
        .append('g')
        .classed(Styles.FLOWS, true);

    const nodes = container
        .append('g')
        .classed(Styles.NODES, true);

    return {
        svg,
        container,
        flows,
        nodes
    };
}


export function mkClassSelector(c) {
    return '.'+c;
}


const actorSymbol = symbol()
    .size(256)
    .type(symbolWye);


export function addNodeSymbol(selection) {
    selection
        .filter(n => _.get(n, 'waltzData.kind', 'APPLICATION') === 'APPLICATION')
        .append('circle')
        .classed(Styles.GLYPH, true)
        .attr('r', 12);

    selection
        .filter(n =>  _.get(n, 'waltzData.kind', 'APPLICATION') === 'ACTOR')
        .append('path')
        .classed(Styles.GLYPH, true)
        .attr('d', actorSymbol);

    return selection;
}


export function addNodeLabel(selection) {
    return selection
        .append('text')
        .attr('text-anchor', 'middle')
        .attr('dy', '1.2em')
        .text(d => _.get(d, 'waltzData.name', '?'));
}


// -- model

export function nodeToGraphId(n) {
    return `${n.kind}/${n.id}`;
}


export function flowToGraphId(f) {
    return `LOGICAL_FLOW/${f.id}`;
}


export function concatNodesFromFlows(existing = [], newFlows = []) {
    const flowNodes = _
        .chain(newFlows)
        .flatMap(f => { return [ f.source, f.target ]})
        .map(n => {
            return {
                id: nodeToGraphId(n),
                waltzData: n,
                y: 500
            };
        })
        .uniqBy('id')
        .value();

    return _.unionBy(existing, flowNodes, 'id');
}


export function concatFlows(existing = [], newFlows = []) {
    const flowsToAdd = _.map(newFlows, f => {
        return {
            id: flowToGraphId(f),
            waltzData: f,
            source: nodeToGraphId(f.source),
            target: nodeToGraphId(f.target)
        };
    });
    const simplifiedExisting = _.map(existing, f => {
        return {
            id: f.id,
            waltzData: f.waltzData,
            source: _.isObject(f.source) ? f.source.id : f.source,
            target: _.isObject(f.target) ? f.target.id : f.source,
        };
    });

    return _.unionBy(simplifiedExisting, flowsToAdd, 'id');
}


export function calcDownstreams(graphData, newFlows, centerId) {
    const downstreams = Object.assign({}, graphData.downstreams || {});
    downstreams[centerId] = [];
    const graphFlowsBySourceId = _.groupBy(graphData.flows, 'source')
    const graphNodesById = _.keyBy(graphData.nodes, 'id');

    _.forEach(graphData.nodes, n => {
        const downstreamNodes = _
            .chain(graphFlowsBySourceId[n.id] || [])
            .map('target')
            .uniq()
            .map(nId => graphNodesById[nId])
            .value();
        downstreams[n.id] = downstreamNodes;
    });

    return downstreams;
}