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

import _ from "lodash";
import {initialiseData} from "../common";
import {CORE_API, getApiReference} from '../common/services/core-api-utils';

import template from './entity-named-node-types-view.html';


const initialState = {
    noteTypes: [],
    creatingNoteType: false,
    newNoteType: { }
};


function splitEntityKinds(entityKinds) {
    return _.split(entityKinds, /,\s*/);
}


function controller($q,
                    notification,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const componentId = 'entity-named-note-types-view';

    function update(id, change) {
        return serviceBroker
            .execute(CORE_API.EntityNamedNoteTypeStore.update, [id, change])
            .then(() => {
                loadNoteTypes(true);
                notification.success('Updated');
            });
    }

    vm.updateName = (id, change) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(id, { name: change.newVal });
    };

    vm.updateDescription = (id, change) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(id, { description: change.newVal });
    };

    vm.updateIsReadOnly = (id, change) => {
        return update(id, { isReadOnly: change.newVal });
    };

    vm.updateApplicableEntityKinds = (id, change) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(id, { applicableEntityKinds: splitEntityKinds(change.newVal) });
    };

    vm.startNewNoteType = () => {
        vm.creatingNoteType = true;
    };

    vm.saveNewNoteType = () => {
        const params = [{
            name: vm.newNoteType.name,
            description: vm.newNoteType.description,
            applicableEntityKinds: splitEntityKinds(vm.newNoteType.applicableEntityKinds),
            isReadOnly: vm.newNoteType.isReadOnly
        }];

        return serviceBroker
            .execute(CORE_API.EntityNamedNoteTypeStore.create, params)
            .then(() => {
                notification.success('Created new note type: '+ vm.newNoteType.name);
                vm.creatingNoteType = false;
                vm.newNoteType = {};
                loadNoteTypes(true);
            });
    };

    vm.deleteNoteType = (id) => {
        if (confirm('Are you sure you want to delete this note type?')) {
            return serviceBroker
                .execute(CORE_API.EntityNamedNoteTypeStore.remove, [id])
                .then((r) => {
                    if (r.data) {
                        notification.success('Deleted');
                        loadNoteTypes(true);
                    } else {
                        notification.error('Failed to delete, ensure that note type is not being used');
                    }
                });
        }
    };

    vm.cancelNewNoteType = () => {
        vm.creatingNoteType = false;
    };


    function loadNoteTypes(force = false) {
        const options = {
            force,
            cacheRefreshListener: {
                componentId,
                fn: cacheRefreshListener
            }
        };

        serviceBroker
            .loadAppData(
                CORE_API.EntityNamedNoteTypeStore.findAll,
                [],
                options)
            .then(result => {
                vm.noteTypes = result.data;
            });
    }

    const cacheRefreshListener = (e) => {
        if (e.eventType === 'REFRESH'
            && getApiReference(e.serviceName, e.serviceFnName) === CORE_API.EntityNamedNoteTypeStore.findAll) {
            loadNoteTypes();
        }
    };

    loadNoteTypes();
}


controller.$inject = [
    '$q',
    'Notification',
    'ServiceBroker'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
