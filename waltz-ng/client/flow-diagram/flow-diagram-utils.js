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




export function createSampleDiagram() {
    const birman = { kind: 'APPLICATION', id: 22253, name: "Birman"};
    const bear = { kind: 'APPLICATION', id: 22276, name: "Bear"};
    const cassowary = { kind: 'APPLICATION', id: 22666, name: "Cassowary"};
    const architect = { kind: 'ACTOR', id: 1, name: "Architect with a very long name"};

    const bearToBirman = { kind: 'LOGICAL_FLOW', id: 45878, source: bear, target: birman };
    const birmanToCassowary = { kind: 'LOGICAL_FLOW', id: 45482, source: birman, target: cassowary };
    const cassowaryToArchitect = { kind: 'LOGICAL_FLOW', id: 1, source: cassowary, target: architect };

    const secrets = { kind: 'PHYSICAL_SPECIFICATION', id: 25091, name: "Secrets"};
    const transferHoldings = { id: 16593, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-holdings.tsv' };
    const transferPurchases = { id: 16594, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-purchases.tsv' };

    const birmanPhysicalFlow1 = { kind: 'PHYSICAL_FLOW', id: 25092, specification: transferHoldings };
    const birmanPhysicalFlow2 = { kind: 'PHYSICAL_FLOW', id: 25092, specification: transferPurchases };
    const cassowaryPhysicalFlow = { kind: 'PHYSICAL_FLOW', id: 25091, specification: secrets };

    const birmanToCassowaryDecoration1 = { ref: { id: 45482, kind: 'LOGICAL_FLOW' }, decoration: birmanPhysicalFlow1 };
    const birmanToCassowaryDecoration2 = { ref: { id: 45482, kind: 'LOGICAL_FLOW' }, decoration: birmanPhysicalFlow2 };
    const cassowaryToArchitectDecoration = { ref: { id: 1, kind: 'LOGICAL_FLOW' }, decoration: cassowaryPhysicalFlow };

    const bearAnnotation = {
        id: 2, ref: {id: 22276, kind: 'APPLICATION' },
        note: "However you choose to use LOOPY, hopefully it can give you not just the software tools, but also the mental tools to understand the complex systems of the world around us. It's a hot mess out there. ",
        dy: 37, dx: -62 };

    const architectAnnotation = {
        id: 1, ref: {id: 1, kind: 'ACTOR' },
        note: "An architect, probably very clever",
        dy: 37, dx: -62 };

    return [
        { command: 'ADD_NODE', payload: birman },
        { command: 'ADD_NODE', payload: cassowary },
        { command: 'ADD_NODE', payload: architect },
        { command: 'ADD_NODE', payload: bear },

        { command: 'ADD_FLOW', payload: birmanToCassowary },
        { command: 'ADD_FLOW', payload: cassowaryToArchitect },
        { command: 'ADD_FLOW', payload: bearToBirman },

        { command: 'ADD_DECORATION', payload: birmanToCassowaryDecoration1 },
        { command: 'ADD_DECORATION', payload: birmanToCassowaryDecoration2 },
        { command: 'ADD_DECORATION', payload: cassowaryToArchitectDecoration },
        { command: 'ADD_ANNOTATION', payload: bearAnnotation },
        { command: 'ADD_ANNOTATION', payload: architectAnnotation }
    ];
}