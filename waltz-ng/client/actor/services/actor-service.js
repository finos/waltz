
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
