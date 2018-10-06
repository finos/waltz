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
import apiCheck from "api-check";
import {notEmpty} from "./index";


const myApiCheck = apiCheck({ verbose: false });


const entityRefShape = {
    id: apiCheck.number,
    kind: apiCheck.string
};


const measurableRatingShape = {
    entityReference: myApiCheck.shape(entityRefShape),
    measurableId: apiCheck.number,
    rating: apiCheck.string,
};


const logicalFlowShape = {
    source: myApiCheck.shape(entityRefShape),
    target: myApiCheck.shape(entityRefShape),
    id: apiCheck.number
};


const measurableShape = {
    id: apiCheck.number,
    categoryId: apiCheck.number,
    name: apiCheck.string,
    description: apiCheck.string
};


const idSelectorShape = {
    entityReference: myApiCheck.shape(entityRefShape),
    scope: myApiCheck.oneOf(["EXACT", "PARENTS", "CHILDREN"])
};


const specificationShape = {
    name: apiCheck.string,
    description: apiCheck.string,
    format: apiCheck.string,
    lastUpdatedBy: apiCheck.string
};

const flowAttributesShape = {
    transport: apiCheck.string,
    frequency: apiCheck.string,
    basisOffset: apiCheck.number,
};

const componentShape = {
    template: apiCheck.string,
    controller: apiCheck.func.optional,
    bindings: apiCheck.object
};

const componentDefinitionShape = {
    id: apiCheck.string,
    component: myApiCheck.shape(componentShape)
};

const storeDefinitionShape = {
    serviceName: apiCheck.string,
    store: myApiCheck.func
};

const serviceDefinitionShape = {
    serviceName: apiCheck.string,
    service: myApiCheck.func
};

const serviceBrokerTargetShape = {
    serviceName: apiCheck.string,
    serviceFnName: apiCheck.string
};

const authSourceUpdateCommand = {
    description: apiCheck.string.optional,
    rating: apiCheck.string,
    id: apiCheck.number
};

const authSourceCreateCommand = {
    description: apiCheck.string.optional,
    rating: apiCheck.string,
    applicationId: apiCheck.number,
    dataTypeId: apiCheck.number,
    orgUnitId: apiCheck.number
};

const serviceBrokerCacheRefreshListenerShape = {
    componentId: apiCheck.string,
    fn: myApiCheck.func
};

const serviceBrokerOptionsShape = {
    force: apiCheck.bool.optional,
    cacheRefreshListener: myApiCheck.shape(serviceBrokerCacheRefreshListenerShape).optional
};

const dynamicSectionShape = {
    id: apiCheck.number,
    componentId: apiCheck.string,
    name: apiCheck.string,
    icon: apiCheck.string
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


const entityInvolvementChangeCommandShape = {
    operation: apiCheck.string,
    personEntityRef: myApiCheck.shape(entityRefShape),
    involvementKindId: apiCheck.number
};


const createPhysicalFlowCommandShape = {
    specification: myApiCheck.shape(specificationShape),
    flowAttributes: myApiCheck.shape(flowAttributesShape),
    logicalFlowId: apiCheck.number
};


const entityRelationshipChangeCommandShape = {
    operation: apiCheck.string,
    entityReference: myApiCheck.shape(entityRefShape),
    relationship: apiCheck.string
};


// -- CHECKERS

const check = (test, x) => myApiCheck.throw(test, x);

const assert = (b, msg) => { if (!b) throw msg; };


export function checkTrue(b, msg) {
    assert(b, msg);
}

export function checkNotEmpty(x, msg = "is empty") {
    assert(notEmpty(x), msg);
}

export const checkIsEntityRef = ref =>
    check(myApiCheck.shape(entityRefShape), ref);


export const checkIsLogicalFlow = flow =>
    check(myApiCheck.shape(logicalFlowShape), flow);


export const checkIsMeasurableRating = ref =>
    check(myApiCheck.shape(measurableRatingShape), ref);


export const checkIsMeasurable = ref =>
    check(myApiCheck.shape(measurableShape), ref);


export const checkIsCreatePhysicalFlowCommand = cmd =>
    check(myApiCheck.shape(createPhysicalFlowCommandShape), cmd);


export const checkIsCreateActorCommand = cmd =>
    check(myApiCheck.shape(createActorCommandShape), cmd);


export const checkIsCreateInvolvementKindCommand = ref =>
    check(myApiCheck.shape(createInvolvementKindCommandShape), ref);


export const checkIsEntityInvolvementChangeCommand = ref => {
    check(myApiCheck.shape(entityInvolvementChangeCommandShape), ref);
};


export const checkIsEntityRelationshipChangeCommand = ref => {
    check(myApiCheck.shape(entityRelationshipChangeCommandShape), ref);
};

export const checkIsComponentDefinition = (def) => {
    check(myApiCheck.shape(componentDefinitionShape), def);
};

export const checkIsStoreDefinition = (def) => {
    check(myApiCheck.shape(storeDefinitionShape), def);
};

export const checkIsServiceDefinition = (def) => {
    check(myApiCheck.shape(serviceDefinitionShape), def);
};

export const checkIsServiceBrokerTarget = (target) => {
    check(myApiCheck.shape(serviceBrokerTargetShape), target);
};

export const checkIsAuthSourceUpdateCommand = (target) => {
    check(myApiCheck.shape(authSourceUpdateCommand), target);
};

export const checkIsAuthSourceCreateCommand = (target) => {
    check(myApiCheck.shape(authSourceCreateCommand), target);
};

export const checkIsServiceBrokerOptions = (options) => {
    check(myApiCheck.shape(serviceBrokerOptionsShape), options);
};

export const checkIsDynamicSection = (section) => {
    check(myApiCheck.shape(dynamicSectionShape), section);
};

export const checkIsStringList = xs =>
    check(myApiCheck.arrayOf(apiCheck.string), xs);


/* @Deprecated - use checkIsIdSelector instead */
export const checkIsApplicationIdSelector = opt =>
    check(
        myApiCheck.shape(idSelectorShape),
        opt);


export const checkIsIdSelector = checkIsApplicationIdSelector;


export function checkAll(xs, pred = x => true, msg = "failed test") {
    return assert(_.every(xs, pred), msg);
}


export function checkIsArray(xs, message) {
    if (!_.isArray(xs)) {
        throw new Error(message ? message : "not an array", xs);
    } else {
        return xs;
    }
}


export function ensureNotNull(x, message) {
    if (_.isNull(x) || _.isUndefined(x)) {
        throw new Error(message ? message : "is null", x);
    } else {
        return x;
    }
}

