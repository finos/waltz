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
import {checkIsEntityRef} from "./checks";
import {CORE_API} from "./services/core-api-utils";
import {applicationStore} from "../svelte-stores/application-store";
import {actorStore} from "../svelte-stores/actor-store";
import {endUserApplicationStore} from "../svelte-stores/end-user-application-store";

export function sameRef(r1, r2, options = { skipChecks: false }) {
    if (! options.skipChecks) {
        checkIsEntityRef(r1);
        checkIsEntityRef(r2);
    }
    return r1.kind === r2.kind && r1.id === r2.id;
}


export function isSameParentEntityRef(changes) {
    return sameRef(
        changes.parentEntityRef.previousValue,
        changes.parentEntityRef.currentValue,
        {skipChecks: true});
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


export function toEntityRef(obj) {
    return {
        id: obj.id,
        kind: obj.kind,
        name: obj.name,
        externalId: obj.externalId,
        description: obj.description,
        entityLifecycleStatus: obj.entityLifecycleStatus
    };
}


export function toEntityRefWithKind(obj, kind) {
    return toEntityRef(Object.assign({}, obj, {kind}));
}


export function mkRef(kind, id, name, description) {
    return {
        kind,
        id,
        name,
        description
    };
}


function determineLoadByIdCall(kind) {
    switch (kind) {
        case "APPLICATION":
            return CORE_API.ApplicationStore.getById;
        case "ACTOR":
            return CORE_API.ActorStore.getById;
        case "CHANGE_INITIATIVE":
            return CORE_API.ChangeInitiativeStore.getById;
        case "ORG_UNIT":
            return CORE_API.OrgUnitStore.getById;
        case "MEASURABLE":
            return CORE_API.MeasurableStore.getById;
        case "APP_GROUP":
            return CORE_API.AppGroupStore.getById;
        case "END_USER_APPLICATION":
            return CORE_API.EndUserAppStore.getById;
        default:
            throw "Unsupported kind for loadById: " + kind;
    }
}


function determineLoadByExtIdCall(kind) {
    switch (kind) {
        case "APPLICATION":
            return CORE_API.ApplicationStore.findByAssetCode;
        case "DATA_TYPE":
            return CORE_API.DataTypeStore.getDataTypeByCode;
        case "MEASURABLE":
            return CORE_API.MeasurableStore.findByExternalId;
        case "PERSON":
            return CORE_API.PersonStore.getByEmployeeId;
        case "PHYSICAL_FLOW":
            return CORE_API.PhysicalFlowStore.findByExternalId;
        default:
            throw "Unsupported kind for loadByExtId: " + kind;
    }
}


function getEntityFromData(kind, data) {
    switch (kind) {
        case "APP_GROUP":
            return data.appGroup;
        default:
            return data;
    }
}


export function loadEntity(serviceBroker, entityRef) {
    checkIsEntityRef(entityRef);

    const remoteCall = determineLoadByIdCall(entityRef.kind);
    return serviceBroker
        .loadViewData(remoteCall, [entityRef.id])
        .then(r => getEntityFromData(entityRef.kind, r.data));
}


export function loadSvelteEntity(entityRef, force = false) {
    const id = entityRef.id;
    switch (entityRef.kind) {
        case "APPLICATION":
            return applicationStore.getById(id, force);
        case "ACTOR":
            return actorStore.getById(id, force);
        case "END_USER_APPLICATION":
            return endUserApplicationStore.getById(id, force);
        default:
            throw "Don't know how to load entity for kind: " + entityRef.kind;
    }
}

export function loadByExtId(serviceBroker, kind, extId) {
    try {
        const remoteCall = determineLoadByExtIdCall(kind);
        return serviceBroker
            .loadViewData(remoteCall, [extId])
            .then(r => _.defaultTo(r.data, []))
            .then(d => _.isArray(d) ? d : [d])
    } catch (e) {
        return Promise.reject(e);
    }
}


export function isRemoved(entity) {
    return entity.isRemoved || entity.entityLifecycleStatus === "REMOVED";
}
