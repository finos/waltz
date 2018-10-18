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

import {checkIsEntityRef} from "./checks";
import {CORE_API} from "./services/core-api-utils";

export function sameRef(r1, r2, options = { skipChecks: false }) {
    if (! options.skipChecks) {
        checkIsEntityRef(r1);
        checkIsEntityRef(r2);
    }
    return r1.kind === r2.kind && r1.id === r2.id;
}


export function refToString(r) {
    checkIsEntityRef(r);
    return `${r.kind}/${r.id}`;
}


export function stringToRef(s) {
    const bits = s.split("/");
    return {
        kind: bits[0],
        id: bits[1]
    };
}


export function toEntityRef(obj, kind = obj.kind) {

    const ref = {
        id: obj.id,
        kind,
        name: obj.name,
        description: obj.description
    };

    checkIsEntityRef(ref);

    return ref;
}


function determineLoadByIdCall(kind) {
    switch (kind) {
        case "APPLICATION":
            return CORE_API.ApplicationStore.getById;
        case "ACTOR":
            return CORE_API.ActorStore.getById;
        default:
            throw "Unsupported kind for loadEntity: " + kind;
    }
}


export function loadEntity(serviceBroker, entityRef) {
    checkIsEntityRef(entityRef);

    const remoteCall = determineLoadByIdCall(entityRef.kind);
    return serviceBroker
        .loadViewData(remoteCall, [ entityRef.id ])
        .then(r => r.data);
}

