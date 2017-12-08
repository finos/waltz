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

import {checkIsEntityRef} from './checks';

export function sameRef(r1, r2) {
    checkIsEntityRef(r1);
    checkIsEntityRef(r2);
    return r1.kind === r2.kind && r1.id === r2.id;
}


export function refToString(r) {
    checkIsEntityRef(r);
    return `${r.kind}/${r.id}`;
}


export function stringToRef(s) {
    const bits = s.split('/');
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

