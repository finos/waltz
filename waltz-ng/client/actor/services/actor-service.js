
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

let actorsPromise = null;

function service(actorStore) {

    const loadActors = (force = false) => {
        if (force || (actorsPromise == null)) {
            actorsPromise = actorStore.findAll();
        }
        return actorsPromise;
    };


    const update = (command) => {
        if(!actorsPromise || command === undefined || command === null) return;

        return actorStore
            .update(command)
            .then(response => {
                if(response.outcome === "SUCCESS") {
                    return loadActors(true)
                        .then(kinds => true);
                } else {
                    throw "could not update: " + command;
                }
            });
    };


    const deleteById = (id) => {
        return actorStore
            .deleteById(id)
            .then(status => {
                return loadActors(true)
                    .then(kinds => status);
            });
    };


    const create = (cmd) => {
        return actorStore
            .create(cmd)
            .then(createdId => {
                if(createdId > 0) {
                    return loadActors(true)
                        .then((kinds) => createdId);
                } else {
                    throw "could not create: " + cmd;
                }
            });
    };


    loadActors();

    return {
        loadActors,
        update,
        deleteById,
        create
    };
}


service.$inject = [
    'ActorStore'
];


export default service;
