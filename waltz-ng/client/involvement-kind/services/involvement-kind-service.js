
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
