/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import apiCheck from "api-check";


const myApiCheck = apiCheck({ verbose: false });


const entityRefShape = {
    id: apiCheck.number,
    kind: apiCheck.string
};


const idSelectorShape = {
    entityReference: myApiCheck.shape(entityRefShape),
    scope: myApiCheck.oneOf(['EXACT', 'PARENTS', 'CHILDREN'])
};


const specificationShape = {
    name: apiCheck.string,
    description: apiCheck.string,
    format: apiCheck.string
};

const flowAttributesShape = {
    transport: apiCheck.string,
    frequency: apiCheck.string,
    basisOffset: apiCheck.number,
};


// -- COMMANDS --

const createActorCommandShape = {
    name: apiCheck.string,
    description: apiCheck.string,
    isExternal: apiCheck.bool
};


const createInvolvementKindCommandShape = {
    name: apiCheck.string,
    description: apiCheck.string
};


const createPhysicalFlowCommandShape = {
    specification: myApiCheck.shape(specificationShape),
    flowAttributes: myApiCheck.shape(flowAttributesShape),
    targetEntity: myApiCheck.shape(entityRefShape)
};


// -- CHECKERS

const check = (test, x) => myApiCheck.throw(test, x);


export const checkIsEntityRef = ref =>
    check(myApiCheck.shape(entityRefShape), ref);


export const checkIsCreatePhysicalFlowCommand = cmd =>
    check(myApiCheck.shape(createPhysicalFlowCommandShape), cmd);


export const checkIsCreateActorCommand = cmd =>
    check(myApiCheck.shape(createActorCommandShape), cmd);


export const checkIsCreateInvolvementKindCommand = ref =>
    check(myApiCheck.shape(createInvolvementKindCommandShape), ref);


export const checkIsStringList = xs =>
    check(myApiCheck.arrayOf(apiCheck.string), xs);


/* @Deprecated - use checkIsIdSelector instead */
export const checkIsApplicationIdSelector = opt =>
    check(
        myApiCheck.shape(idSelectorShape),
        opt);


export const checkIsIdSelector = checkIsApplicationIdSelector;


export function ensureIsArray(xs, message) {
    if (!_.isArray(xs)) {
        throw new Error(message ? message : 'not an array', xs);
    } else {
        return xs;
    }
}


export function ensureNotNull(x, message) {
    if (_.isNull(x) || _.isUndefined(x)) {
        throw new Error(message ? message : 'is null', x);
    } else {
        return x;
    }
}


export function ensureIsNumber(x, message) {
    const num = Number(x);
    if (_.isNaN(num)) {
        throw new Error(message ? message : `${x} is not a number`, x);
    } else {
        return num;
    }
}
