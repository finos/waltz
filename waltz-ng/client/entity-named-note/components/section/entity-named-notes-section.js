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

import _ from 'lodash';
import {CORE_API, getApiReference} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common/index';
import {getEditRoleForEntityKind} from '../../../common/role-utils';

import template from './entity-named-notes-section.html';
import {displayError} from "../../../common/error-utils";


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    availableTypes: [],
    notes: [],
    allNoteTypes: [],
    hasRole: false,
    creatingNote: false,
};


function calcAvailableTypes(parentRef, noteTypes, notes) {
    const usedTypes = _.map(notes, 'namedNoteTypeId');

    return _
        .chain(noteTypes)
        .filter(nt => !nt.isReadOnly)
        .filter(nt => nt.applicableEntityKinds.indexOf(parentRef.kind) !== -1)
        .filter(nt => !_.includes(usedTypes, nt.id))
        .value();
}


function controller($q, notification, serviceBroker, userService) {
    const vm = initialiseData(this, initialState);
    const componentId = 'entity-named-notes-section';

    const recalcAvailableTypes = () => {
        vm.availableTypes = calcAvailableTypes(
            vm.parentEntityRef,
            vm.allNoteTypes,
            vm.notes);
    };

    vm.$onInit = () => {
        if (vm.parentEntityRef) {
            loadAll()
                .then(recalcAvailableTypes);
        }

        const role = getEditRoleForEntityKind(vm.parentEntityRef.kind, "ENTITY_NAMED_NOTE");
        userService
            .whoami()
            .then(user => vm.hasRole = userService.hasRole(user, role));
    };

    const loadNamedNotes = (force = false) => {
        const options = {
            force,
            cacheRefreshListener: {
                componentId,
                fn: cacheRefreshListener
            }
        };

        return serviceBroker
            .loadViewData(CORE_API.EntityNamedNoteStore.findByEntityReference, [vm.parentEntityRef], options)
            .then(result => vm.notes = result.data)
            .catch(e => displayError(notification, "Failed to load data", e));
    };

    const loadNoteTypes = () => {
        const options = {
            cacheRefreshListener: {
                componentId,
                fn: cacheRefreshListener
            }
        };

        return serviceBroker
            .loadAppData(CORE_API.EntityNamedNoteTypeStore.findAll, [], options)
            .then(result => vm.allNoteTypes = result.data);
    };

    const loadAll = (force = false) => {
        return $q
            .all([loadNamedNotes(force), loadNoteTypes()]);
    };

    const cacheRefreshListener = (e) => {
        if (e.eventType === 'REFRESH'
            && getApiReference(e.serviceName, e.serviceFnName) === CORE_API.EntityNamedNoteStore.findByEntityReference) {

            loadNamedNotes();
        }
    };

    vm.saveNote = (note) => {
        const params = [vm.parentEntityRef, note.namedNoteTypeId, {newStringVal: note.noteText}];

        return serviceBroker
            .execute(CORE_API.EntityNamedNoteStore.save,
                params)
            .then(rc => {
                if (rc) {
                    notification.success('Note saved successfully');
                } else {
                    notification.error('Failed to save note');
                }
            })
            .then(() => loadNamedNotes(true))
            .then(recalcAvailableTypes);
    };

    vm.deleteNote = (note) => {
        const params = [vm.parentEntityRef, note.namedNoteTypeId];

        return serviceBroker.execute(CORE_API.EntityNamedNoteStore.remove, params)
            .then(rc => {
                if (rc) {
                    notification.success('Note deleted successfully');
                } else {
                    notification.error('Failed to delete note');
                }
            })
            .then(() => loadNamedNotes(true))
            .then(recalcAvailableTypes);
    };

    vm.editorDismiss = () => {
        vm.creatingNote = false;
    };

    vm.showEditor = () => {
        vm.creatingNote = true;
    };

}


controller.$inject = [
    '$q',
    'Notification',
    'ServiceBroker',
    'UserService'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzEntityNamedNotesSection'
};
