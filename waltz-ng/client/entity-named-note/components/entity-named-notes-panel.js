import _ from "lodash";
import {initialiseData} from "../../common";
import {CORE_API, getApiReference} from '../../common/services/core-api-utils';

import template from './entity-named-notes-panel.html';


const bindings = {
    parentEntityRef: '<',
    editRole: '@'
};


const initialState = {
    creatingNote: false,
    creatableNoteTypes: [],
    editingNotes: {},
    expandedNotes: {},
    newNote: {},
    baseLabelText: 'Show Additional Notes',
    labelText: 'Show Additional Notes',
    visibility: {notes: false}
};


function controller($q, serviceBroker, notification) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.parentEntityRef) {
            loadAll();
        }
    };

    const postLoad = () => {
        const existingNotesByTypeId = _.keyBy(vm.notes, 'namedNoteTypeId');

        vm.creatableNoteTypes = _.chain(vm.allNoteTypes)
            .filter(nt => !nt.isReadOnly)
            .filter(nt => !existingNotesByTypeId[nt.id])
            .filter(nt => nt.applicableEntityKinds.indexOf(vm.parentEntityRef.kind) !== -1)
            .value();

        vm.noteTypesById = _.keyBy(vm.allNoteTypes, 'id');
        if (vm.notes.length === 0) {
            vm.visibility.notes = false;
        } else {
            const noteTitles = _.chain(vm.notes)
                .map(n => vm.noteTypesById[n.namedNoteTypeId])
                .map(nt => nt.name).value();

            vm.labelText = vm.baseLabelText + ': ' + _.truncate(_.join(noteTitles, ', '), {length: 200});
        }
    };

    const loadNamedNotes = (force = false) => {
        const options = {
            force,
            cacheRefreshListener: cacheRefreshListener
        }

        return serviceBroker
            .loadViewData(CORE_API.EntityNamedNoteStore.findByEntityReference, [vm.parentEntityRef], options)
            .then(result => vm.notes = result.data)
            .catch(error => notification.error('Failed to load data: ', error));
    };

    const loadNoteTypes = () => {
        const options = {
            cacheRefreshListener: cacheRefreshListener
        }

        return serviceBroker
            .loadViewData(CORE_API.EntityNamedNoteTypeStore.findAll, [], options)
            .then(result => vm.allNoteTypes = result.data);
    };

    const loadAll = (force = false) => {
        return $q
            .all([loadNamedNotes(force), loadNoteTypes()])
            .then(() => postLoad());
    };

    const cacheRefreshListener = (e) => {
        if (e.eventType === 'REFRESH'
            && getApiReference(e.serviceName, e.serviceFnName) === CORE_API.EntityNamedNoteStore.findByEntityReference) {

            loadNamedNotes()
                .then(postLoad);
        }
    };

    const saveNote = (note) => {
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

    const deleteNote = (note) => {
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

    vm.showNotes = () => {
        vm.visibility.notes = true;
    };

    vm.startNewNote = () => {
        vm.creatingNote = true;
    };

    vm.cancelNewNote = () => {
        vm.creatingNote = false;
    };

    vm.saveNewNote = () => {
        return saveNote(vm.newNote).then(() => {
            vm.newNote = {};
            vm.creatingNote = false;
            vm.visibility.notes = true;
        });
    };

    vm.startEditNote = (note) => {
        vm.editingNotes[note.namedNoteTypeId] = {noteTextVal: note.noteText};
    };

    vm.cancelEditNote = (note) => {
        vm.editingNotes[note.namedNoteTypeId] = null;
    };

    vm.updateNote = (note) => {
        const newNote = {
            namedNoteTypeId: note.namedNoteTypeId,
            noteText: vm.editingNotes[note.namedNoteTypeId].noteTextVal
        };
        saveNote(newNote).then(() => {
            vm.editingNotes[note.namedNoteTypeId] = null;
        });
    };

    vm.deleteNote = (note) => {
        if (confirm('Are you sure you want to delete this note?')) {
            deleteNote(note);
        }
    };

    vm.expandNote = (note) => vm.expandedNotes[note.namedNoteTypeId] = true;

    vm.collapseNote = (note) => vm.expandedNotes[note.namedNoteTypeId] = false;
}


controller.$inject = [
    '$q',
    'ServiceBroker',
    'Notification'
];


const component = {
    template,
    bindings,
    controller
};


export default component;
