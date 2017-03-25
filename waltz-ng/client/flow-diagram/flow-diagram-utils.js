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


export function mkModel(nodes = [], flows = []) {
    return {
        nodes: _.map(nodes, toGraphNode),
        flows: _.map(flows, toGraphFlow),
        decorations: {}
    };
}

// -- shapes

const paperShape = {
    path: 'M0,0 L90,0 L100,10 L100,20 L0,20 z',
    cx: 50,
    cy: 10,
    title: {
        dx: 6,
        dy: 13
    }
};


function mkTrapezoidShape(widthHint) {
    return {
        path: `M0,0 L${widthHint},0 L${widthHint - 5},20 L5,20 z`,
        cx: widthHint / 2,
        cy: 10,
        title: {
            dx: 8,
            dy: 13
        }
    };
}


function mkRectShape(widthHint) {
    const shape = {
        path: `M0,0 L${widthHint},0 L${widthHint},20 L0,20 z`,
        cx: widthHint / 2,
        cy: 10,
        title: {
            dx: 4,
            dy: 13
        }
    };
    return shape
}


const shapes = {
    ACTOR: (widthHint = 100) => Object.assign({}, mkTrapezoidShape(widthHint), { icon: '\uf2be'}),
    APPLICATION: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: '\uf108' }),
    DEFAULT: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: '\uf096' })
};


/**
 * Given a nodes model element kind (node.data.kind) will return
 * an object describing a shape which represents the node.
 * The object contains `{ path: '', cx, cy, title: { dx, dy } }`
 * where cx,cy are the center points of the shape.  Title dx,dy
 * give offsets to locate the title in an appropriate position.
 *
 * @param kind
 * @returns {*}
 */
export function toNodeShape(d, widthHint = 100) {
    const kind = _.isObject(d) ? d.kind : d;
    const mkShapeFn = shapes[kind];
    return mkShapeFn(widthHint);
}

