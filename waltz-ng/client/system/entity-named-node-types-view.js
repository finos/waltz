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

import _ from "lodash";
import {initialiseData} from "../common";

const initialState = {
    noteTypes: [],
    creatingNoteType: false,
    newNoteType: { }
};


function splitEntityKinds(entityKinds) {
    return _.split(entityKinds, /,\s*/);
}


function controller($q,
                    entityNamedNoteTypeService,
                    notification) {

    const vm = initialiseData(this, initialState);

    function update(id, change) {
        return entityNamedNoteTypeService.update(id, change)
            .then(() => notification.success('Updated'));
    }

    vm.updateName = (id, change) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(id, { name: change.newVal })
            .then(() => loadNoteTypes());
    };

    vm.updateDescription = (id, change) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(id, { description: change.newVal })
            .then(() => loadNoteTypes());
    };

    vm.updateIsReadOnly = (id, change) => {
        return update(id, { isReadOnly: change.newVal })
            .then(() => loadNoteTypes());
    };

    vm.updateApplicableEntityKinds = (id, change) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(id, { applicableEntityKinds: splitEntityKinds(change.newVal) })
            .then(() => loadNoteTypes());
    };

    vm.startNewNoteType = () => {
        vm.creatingNoteType = true;
    };

    vm.saveNewNoteType = () => {
        entityNamedNoteTypeService
            .create({
                name: vm.newNoteType.name,
                description: vm.newNoteType.description,
                applicableEntityKinds: splitEntityKinds(vm.newNoteType.applicableEntityKinds),
                isReadOnly: vm.newNoteType.isReadOnly
            })
            .then(() => {
                notification.success('Created new note type: '+ vm.newNoteType.name);
                vm.creatingNoteType = false;
                vm.newNoteType = {};
                loadNoteTypes();
            });


    };

    vm.deleteNoteType = (id) => {
        if (confirm('Are you sure you want to delete this note type?')) {
            entityNamedNoteTypeService
                .remove(id)
                .then(r => {
                    if (r) {
                        notification.success('Deleted');
                        loadNoteTypes();
                    } else {
                        notification.error('Failed to delete, ensure that note type is not being used');
                    }
                });
        }
    };

    vm.cancelNewNoteType = () => {
        vm.creatingNoteType = false;
    };


    function loadNoteTypes() {
        entityNamedNoteTypeService
            .loadNoteTypes()
            .then(noteTypes => {
                vm.noteTypes = noteTypes;
            });
    }

    loadNoteTypes();
}


controller.$inject = [
    '$q',
    'EntityNamedNoteTypeService',
    'Notification'
];


export default {
    template: require('./entity-named-node-types-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
