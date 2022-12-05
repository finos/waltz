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

const flowClassificationRuleUpdateCommand = {
    description: apiCheck.string.optional,
    classificationId: apiCheck.number,
    id: apiCheck.number
};

const flowClassificationRuleCreateCommand = {
    description: apiCheck.string.optional,
    classificationId: apiCheck.number,
    subjectReference: apiCheck.shape(entityRefShape),
    dataTypeId: apiCheck.number,
    parentReference: apiCheck.shape(entityRefShape)
};

const customEnvironmentShape = {
    name: apiCheck.string,
    owningEntity: myApiCheck.shape(entityRefShape),
    externalId: apiCheck.string,
};

const customEnvironmentUsageShape = {
    customEnvironmentId: apiCheck.number,
    entityReference: myApiCheck.shape(entityRefShape),
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
    componentId: apiCheck.string.optional,
    svelteComponent: apiCheck.func.optional,
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

const assert = (b, msg) => { if (!b) throw _.isFunction(msg) ? msg() : msg; };


export function checkTrue(b, msg) {
    assert(b, msg);
}

export function checkNotEmpty(x, msg = "is empty") {
    assert(notEmpty(x), msg);
}


export function checkIsEntityRef(ref) {
    const hasKind = _.isString(ref.kind);
    const hasId = _.isNumber(ref.id);
    if (hasKind && hasId) {
        // nop
    } else {
        throw `Ref: ${JSON.stringify(ref)} does not look like an entity ref`;
    }
}


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
    checkNotEmpty(target, "Service broker target resolves to null");
    check(myApiCheck.shape(serviceBrokerTargetShape), target);
};

export const checkIsFlowClassificationRuleUpdateCommand = (target) => {
    check(myApiCheck.shape(flowClassificationRuleUpdateCommand), target);
};

export const checkIsFlowClassificationRuleCreateCommand = (target) => {
    check(myApiCheck.shape(flowClassificationRuleCreateCommand), target);
};

export const checkIsCustomEnvironment = (target) => {
    check(myApiCheck.shape(customEnvironmentShape), target);
};

export const checkIsCustomEnvironmentUsage = (target) => {
    check(myApiCheck.shape(customEnvironmentUsageShape), target);
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

