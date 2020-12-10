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

import {CORE_API} from "./services/core-api-utils";

export const allowedRelationshipsByKind = {
    "CHANGE_INITIATIVE": [{
        value: "APPLICATION_NEW",
        name: "Application - new"
    }, {
        value: "APPLICATION_FUNCTIONAL_CHANGE",
        name: "Application - functional change"
    }, {
        value: "APPLICATION_DECOMMISSIONED",
        name: "Application - decommissioned"
    }, {
        value: "APPLICATION_NFR_CHANGE",
        name: "Application - NFR change"
    }, {
        value: "DATA_PUBLISHER",
        name: "Data publisher"
    }, {
        value: "DATA_CONSUMER",
        name: "Data consumer"
    }]
};


export const fetchRelationshipFunctionsByKind = {
    "CHANGE_INITIATIVE": CORE_API.ChangeInitiativeStore.findRelatedForId
};


export const changeRelationshipFunctionsByKind = {
    "CHANGE_INITIATIVE": CORE_API.ChangeInitiativeStore.changeRelationship
};


export function mkRel(a, relationship, b) {
    if (a.kind === "ROADMAP") {
        const tmp = a;
        a = b;
        b = tmp;
    }
    return {a, b, relationship};
}
