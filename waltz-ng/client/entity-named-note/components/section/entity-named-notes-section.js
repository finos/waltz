import _ from 'lodash';
import {CORE_API, getApiReference} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common/index';
import {getEditRoleForEntityKind} from '../../../common/role-utils';

import template from './entity-named-notes-section.html';


const bindings = {
    parentEntityRef: '<',
};


const initialState = {
    notes: [],
    allNoteTypes: [],
    hasRole: false,
    creatingNote: false
};


function controller($q, notification, serviceBroker, userService) {
    const vm = initialiseData(this, initialState);
    const componentId = 'entity-named-notes-section';

    vm.$onInit = () => {
        if (vm.parentEntityRef) {
            loadAll()
                .then(() => {
                    vm.hasCreatableNoteTypes = _.chain(vm.allNoteTypes)
                        .filter(nt => !nt.isReadOnly)
                        .filter(nt => nt.applicableEntityKinds.indexOf(vm.parentEntityRef.kind) !== -1)
                        .value().length > 0;
                });
        }

        const role = getEditRoleForEntityKind(vm.parentEntityRef.kind);
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
            .catch(error => notification.error('Failed to load data: ', error));
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
                    loadNamedNotes(true);
                } else {
                    notification.error('Failed to save note');
                }
            });
    };

    vm.deleteNote = (note) => {
        const params = [vm.parentEntityRef, note.namedNoteTypeId];

        return serviceBroker.execute(CORE_API.EntityNamedNoteStore.remove, params)
            .then(rc => {
                if (rc) {
                    notification.success('Note deleted successfully');
                    loadNamedNotes(true);
                } else {
                    notification.error('Failed to delete note');
                }
            })
    };

    vm.showSection = () => {
        return (vm.hasRole && vm.hasCreatableNoteTypes)
            || (vm.notes && vm.notes.length > 0);
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
