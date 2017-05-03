
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

let noteTypesPromise = null;

function service(entityNamedNoteTypeStore) {

    const loadNoteTypes = (force = false) => {
        if (force || (noteTypesPromise === null)) {
            noteTypesPromise = entityNamedNoteTypeStore.findAll();
        }
        return noteTypesPromise;
    };


    const update = (id, command) => {
        if(!noteTypesPromise || command === undefined || command === null) return;

        return entityNamedNoteTypeStore
            .update(id, command)
            .then(response => {
                if(response) {
                    return loadNoteTypes(true)
                        .then(noteTypes => true);
                } else {
                    throw "could not update: " + command;
                }
            });
    };


    const remove = (id) => {
        return entityNamedNoteTypeStore
            .remove(id)
            .then(status => {
                return loadNoteTypes(true)
                    .then(noteTypes => status);
            });
    };


    const create = (cmd) => {
        return entityNamedNoteTypeStore
            .create(cmd)
            .then(createdId => {
                if(createdId > 0) {
                    return loadNoteTypes(true)
                        .then(noteTypes => createdId);
                } else {
                    throw "could not create: " + cmd;
                }
            });
    };


    loadNoteTypes();

    return {
        loadNoteTypes,
        update,
        remove,
        create
    };
}


service.$inject = [
    'EntityNamedNoteTypeStore'
];


export default service;
