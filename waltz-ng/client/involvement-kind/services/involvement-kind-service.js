
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

let involvementKindsPromise = null;

function service(involvementKindStore) {

    const loadInvolvementKinds = (force = false) => {
        if (force || (involvementKindsPromise == null)) {
            involvementKindsPromise = involvementKindStore.findAll();
        }
        return involvementKindsPromise;
    };


    const update = (command) => {
        if(!involvementKindsPromise || command === undefined || command === null) return;

        return involvementKindStore
            .update(command)
            .then(response => {
                if(response.outcome === "SUCCESS") {
                    return loadInvolvementKinds(true)
                        .then(kinds => true);
                } else {
                    throw "could not update: " + command;
                }
            });
    };


    const deleteById = (id) => {
        return involvementKindStore
            .deleteById(id)
            .then(status => {
                return loadInvolvementKinds(true)
                    .then(kinds => status);
            });
    };


    const create = (cmd) => {
        return involvementKindStore
            .create(cmd)
            .then(createdId => {
                if(createdId > 0) {
                    return loadInvolvementKinds(true)
                        .then((kinds) => createdId);
                } else {
                    throw "could not create: " + cmd;
                }
            });
    };


    loadInvolvementKinds();

    return {
        loadInvolvementKinds,
        update,
        deleteById,
        create
    };
}


service.$inject = [
    'InvolvementKindStore'
];


export default service;
