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


export function toGraphId(datum) {
    return `${datum.kind}/${datum.id}`;
}


export function toGraphNode(node) {
    return {
        id: toGraphId(node),
        data: node
    };
}


export function toGraphFlow(flow) {
    return {
        id: toGraphId(flow),
        source: toGraphId(flow.source),
        target: toGraphId(flow.target),
        data: flow
    };
}


export function toGraphDecoration(decoration) {
    return {
        id: toGraphId(decoration),
        flow: toGraphId({ kind: 'LOGICAL_FLOW', id: decoration.logicalFlowId }),
        data: decoration
    };
}


export function mkModel(nodes = [], flows = []) {
    return {
        nodes: _.map(nodes, toGraphNode),
        flows: _.map(flows, toGraphFlow)
    };
}

// -- shapes

const paperShape = {
    path: 'M0,0 L90,0 L100,10 L100,40 L0,40 z',
    cx: 50,
    cy: 20,
    title: {
        dx: 6,
        dy: 12
    }
};


const trapezoidShape = {
    path: 'M0,0 L100,0 L95,40 L5,40 z',
    cx: 50,
    cy: 20,
    title: {
        dx: 6,
        dy: 12
    }
};


const rectShape = {
    path: 'M0,0 L100,0 L100,40 L0,40 z',
    cx: 50,
    cy: 20,
    title: {
        dx: 4,
        dy: 12
    }
};


const shapes = {
    PHYSICAL_SPECIFICATION: Object.assign({}, paperShape, { icon: '\uf016' }),
    ACTOR: Object.assign({}, trapezoidShape, { icon: '\uf2be'}),
    APPLICATION: Object.assign({}, rectShape, { icon: '\uf108' }),
    DEFAULT: Object.assign({}, rectShape, { icon: '\uf096' })
};


/**
 * Given a nodes model element (node.data) will return
 * an object describing a shape which represents the node.
 * The object contains `{ path: '', cx, cy, title: { dx, dy } }`
 * where cx,cy are the center points of the shape.  Title dx,dy
 * give offsets to locate the title in an appropriate position.
 *
 * @param data
 * @returns {*}
 */
export function toNodeShape(data) {
    return shapes[data.kind] || shapes['DEFAULT'];
}

