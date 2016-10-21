
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
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
