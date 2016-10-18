
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
