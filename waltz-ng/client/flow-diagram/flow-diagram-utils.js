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
        case "DATA_TYPE":
        case "LOGICAL_DATA_FLOW":
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


export function shapeFor(ref) {
    switch (ref.kind) {
        case "LOGICAL_DATA_FLOW":
            return {cx: 0, cy: 0};
        default:
            return {cx: 50, cy: 5};
    }
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
