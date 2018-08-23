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
