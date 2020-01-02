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

import {checkIsEntityRef} from "../../common/checks";


export function store($http, base) {
    const BASE = `${base}/measurable-relationship`;

    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };

    const tallyByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${BASE}/${ref.kind}/${ref.id}/tally`)
            .then(r => r.data);
    };

    const remove = (d) => {
        checkIsEntityRef(d.a);
        checkIsEntityRef(d.b);
        const url = `${BASE}/${d.a.kind}/${d.a.id}/${d.b.kind}/${d.b.id}/${d.relationship}`;
        return $http
            .delete(url)
            .then(r => r.data);
    };

    const create = (d) => {
        checkIsEntityRef(d.a);
        checkIsEntityRef(d.b);
        const url = `${BASE}/${d.a.kind}/${d.a.id}/${d.b.kind}/${d.b.id}/${d.relationshipKind}`;
        return $http
            .post(url , d.description)
            .then(r => r.data);
    };

    const update = (key, changes) => {
        // changes -> { description, relationshipKind }
        checkIsEntityRef(key.a);
        checkIsEntityRef(key.b);
        const url = `${BASE}/${key.a.kind}/${key.a.id}/${key.b.kind}/${key.b.id}/${key.relationshipKind}`;
        return $http
            .put(url, changes)
            .then(r => r.data);
    };

    return {
        findByEntityReference,
        tallyByEntityReference,
        remove,
        create,
        update
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "MeasurableRelationshipStore";


export const MeasurableRelationshipStore_API = {
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "finds relationships by given entity reference"
    },
    tallyByEntityReference: {
        serviceName,
        serviceFnName: "tallyByEntityReference",
        description: "tallies relationships by given entity reference"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "creates a relationship"
    },
    update: {
        serviceName,
        serviceFnName: "update",
        description: "updates a relationship (key, changes)"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "removes a relationship"
    }
};