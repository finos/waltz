/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import _ from "lodash";


export function determineIfCreateAllowed(kind) {
    switch (kind) {
        case "ACTOR":
        case "APPLICATION":
        case "CHANGE_INITIATIVE":
        case "MEASURABLE":
        case "PHYSICAL_FLOW":
        case "PHYSICAL_SPECIFICATION":
            return true;
        default:
            return false;
    }
}


export function toGraphId(datum) {
    if (_.isString(datum)) return datum;
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


export function drawNodeShape(selection, state) {
    selection
        .select("path")
        .attr("d", d => shapeFor(state, d).path)
        .attr("stroke", "#ccc")
        .attr("fill", d => {
            switch (d.data.kind) {
                case "ACTOR":
                    return "#dfd7ee";
                case "APPLICATION":
                    const application = state.detail.applicationsById[d.data.id];
                    if (application) {
                        return application.kind === "EUC" ? "#f1d0d0" : "#dff1d2";
                    } else {
                        return "#dff1d2";
                    }
                default:
                    return "#dff1d2";
            };
        });
}


function initialiseShape(state, graphNode) {
    if (!_.isObject(graphNode)) throw "Cannot initialise shape without an object, was given: " + graphNode;
    const shape = toNodeShape(_.get(graphNode, "data.kind", "DEFAULT"));
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
    ACTOR: (widthHint = 100) => Object.assign({}, mkTrapezoidShape(widthHint), { icon: "\uf2be"}), // user-circle-o
    APPLICATION: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: "\uf108" }),  // desktop
    EUC: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: "\uf109" }), // laptop
    DEFAULT: (widthHint = 100) => Object.assign({}, mkRectShape(widthHint), { icon: "\uf096" })
};


/**
 * Given a nodes model element kind (node.data.kind) will return
 * an object describing a shape which represents the node.
 * The object contains `{ path: '', cx, cy, title: { dx, dy } }`
 * where cx,cy are the center points of the shape.  Title dx,dy
 * give offsets to locate the title in an appropriate position.
 */
export function toNodeShape(d, widthHint = 100) {
    const kind = _.isObject(d) ? d.kind : d;
    const mkShapeFn = shapes[kind];
    if (!mkShapeFn) {
        console.error("Cannot determine shape function for node", d)
    }
    return mkShapeFn(widthHint);
}
