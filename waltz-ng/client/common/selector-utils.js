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


import {checkIsEntityRef} from "./checks";

/**
 * Returns true if the entity kind is hierarchical (has parents/children)
 *
 * @param kind  the entity kind in question (e.g. 'APPLICATION', 'ORG_UNIT')
 * @returns {boolean}  true iff the entity `kind` is hierarchical
 */
export function isHierarchicalKind(kind) {
    switch (kind) {
        case "CHANGE_INITIATIVE":
        case "DATA_TYPE":
        case "ORG_UNIT":
        case "PERSON":
        case "MEASURABLE":
            return true;
        default:
            return false;
    }
}

export function determineDownwardsScopeForKind(kind) {
    switch (kind) {
        case "ACTOR":
        case "APPLICATION":
        case "APP_GROUP":
        case "CHANGE_INITIATIVE":
        case "END_USER_APPLICATION":
        case "FLOW_DIAGRAM":
        case "LICENCE":
        case "LEGAL_ENTITY":
        case "LEGAL_ENTITY_RELATIONSHIP":
        case "LEGAL_ENTITY_RELATIONSHIP_KIND":
        case "LOGICAL_DATA_ELEMENT":
        case "LOGICAL_DATA_FLOW":
        case "MEASURABLE_CATEGORY":
        case "PHYSICAL_FLOW":
        case "PHYSICAL_SPECIFICATION":
        case "PROCESS_DIAGRAM":
        case "SCENARIO":
        case "SERVER":
        case "SOFTWARE":
        case "SOFTWARE_VERSION":
            return "EXACT";
        default:
            return "CHILDREN";
    }
}


export function determineUpwardsScopeForKind(kind) {
    switch (kind) {
        case "ORG_UNIT":
        case "MEASURABLE":
        case "DATA_TYPE":
        case "CHANGE_INITIATIVE":
            return "PARENTS";
        default:
            return "EXACT";
    }
}


/**
 * Helper method to construct valid IdSelectionOption instances.
 * @param entityReference
 * @param scope
 * @param entityLifecycleStatuses
 * @param filters
 * @returns {{entityLifecycleStatuses: string[], entityReference: {kind: *, id: *}, scope: (*|string), filters}}
 */
export function mkSelectionOptions(entityReference,
                                   scope,
                                   entityLifecycleStatuses = ["ACTIVE"],
                                   filters = {}) {
    checkIsEntityRef(entityReference);

    return {
        entityReference: { id: entityReference.id, kind: entityReference.kind }, // use minimal ref to increase cache hits in broker
        scope: scope || determineDownwardsScopeForKind(entityReference.kind),
        entityLifecycleStatuses,
        filters
    };
}


export function mkSelectionOptionsWithJoiningEntity(entityReference,
                                                    scope,
                                                    entityLifecycleStatuses = ["ACTIVE"],
                                                    filters = {},
                                                    joiningEntityKind = null) {
    checkIsEntityRef(entityReference);

    return {
        entityReference: { id: entityReference.id, kind: entityReference.kind }, // use minimal ref to increase cache hits in broker
        scope: scope || determineDownwardsScopeForKind(entityReference.kind),
        entityLifecycleStatuses,
        filters,
        joiningEntityKind: joiningEntityKind
    };
}

