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


function initialiseShape(state, graphNode) {
    if (!_.isObject(graphNode)) throw "Cannot initialise shape without an object, was given: " + graphNode;
    const shape = toNodeShape(_.get(graphNode, 'data.kind', 'DEFAULT'));
    state.layout.shapes[graphNode.id] = shape;
    return shape;
}


function initialisePosition(state, graphNodeId) {
    const position =  { x: 0, y: 0 };
    state.layout.positions[graphNodeId] = position;
    return position;
}


export function positionFor(state, graphNode) {
    const id = _.isString(graphNode)
        ? graphNode
        : graphNode.id;

    return state.layout.positions[id] || initialisePosition(state, id);
}


export function shapeFor(state, graphNode) {
    const id = _.isString(graphNode)
        ? graphNode
        : graphNode.id;

    return state.layout.shapes[id] || initialiseShape(state, graphNode);
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




export function createSampleDiagram(commandProcessor) {
    const mastiff = { kind: 'APPLICATION', id: 23068, name: "23068"};
    const camel = { kind: 'APPLICATION', id: 23061, name: "Camel"};
    const wolfhound = { kind: 'APPLICATION', id: 23062, name: "Irish Wolfhound"};
    const architect = { kind: 'ACTOR', id: 1, name: "Architect with a very long name"};

    const frogToMastiff = { kind: 'LOGICAL_FLOW', id: 45878, source: camel, target: mastiff };
    const mastiffToWolfhound = { kind: 'LOGICAL_FLOW', id: 45482, source: mastiff, target: wolfhound };
    const wolfhoundToArchitect = { kind: 'LOGICAL_FLOW', id: 1, source: wolfhound, target: architect };

    const secrets = { kind: 'PHYSICAL_SPECIFICATION', id: 25091, name: "Secrets"};
    const transferHoldings = { id: 16593, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-holdings.tsv' };
    const transferPurchases = { id: 16594, kind: 'PHYSICAL_SPECIFICATION', name: 'transfer-purchases.tsv' };

    const birmanPhysicalFlow1 = { kind: 'PHYSICAL_FLOW', id: 25092, specification: transferHoldings };
    const birmanPhysicalFlow2 = { kind: 'PHYSICAL_FLOW', id: 25092, specification: transferPurchases };
    const cassowaryPhysicalFlow = { kind: 'PHYSICAL_FLOW', id: 25091, specification: secrets };

    const camelAnnotation = {
        id: 2,
        kind: 'ANNOTATION',
        entityReference: camel,
        note: "However you choose to use LOOPY, hopefully it can give you not just the software tools, but also the mental tools to understand the complex systems of the world around us. It's a hot mess out there. "
    };

    commandProcessor([
        { command: 'ADD_NODE', payload: camel },
        { command: 'ADD_ANNOTATION', payload: camelAnnotation }
    ]);

    commandProcessor(mkMoves());
}


function mkMoves() {
    const positions = {
        "APPLICATION/23068": {
            "x": 389.3717041015625,
            "y": 261.9740295410156
        },
        "APPLICATION/22666": {
            "x": 578.7292175292969,
            "y": 316.3544006347656
        },
        "ACTOR/1": {
            "x": 743.9626770019531,
            "y": 389.3429260253906
        },
        "APPLICATION/22276": {
            "x": 210.69439697265625,
            "y": 227.21038818359375
        },
        "ANNOTATION/1": {
            "x": 16.83001708984375,
            "y": -97.26809692382812
        },
        "ANNOTATION/2": {
            "x": -19.187301635742188,
            "y": 99.41787719726562
        }
    };

    return _.map(positions, (v, k) => {
        return {
            command: 'SET_POSITION',
            payload: {
                x: v.x,
                y: v.y,
                id: k
            }
        }
    })


}