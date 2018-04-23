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
import {initialiseData, invokeFunction} from "../../../common/index";

import template from "./entity-named-notes-panel.html";


const bindings = {
    parentEntityRef: '<',
    notes: '<',
    allNoteTypes: '<',
    onSave: '<',
    onDelete: '<',
    onEditorDismiss: '<',
    creatingNote: '<'
};


const initialState = {
    creatableNoteTypes: [],
    editingNotes: {},
    expandedNotes: {},
    newNote: {},
    baseLabelText: 'Show Additional Notes',
    labelText: 'Show Additional Notes',
    visibility: {notes: true},
    onSave: (note) => console.log('entity named notes panel default save handler: ', note),
    onDelete: (note) => console.log('entity named notes panel default delete handler: ', note),
    onEditorDismiss: () => console.log('entity named notes panel default dismiss editor handler: ', note),
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(vm.notes && vm.allNoteTypes) {
            postLoad();
        }
    };


    const postLoad = () => {
        const existingNotesByTypeId = _.keyBy(vm.notes, 'namedNoteTypeId');

        vm.creatableNoteTypes = _.chain(vm.allNoteTypes)
            .filter(nt => !nt.isReadOnly)
            .filter(nt => !existingNotesByTypeId[nt.id])
            .filter(nt => nt.applicableEntityKinds.indexOf(vm.parentEntityRef.kind) !== -1)
            .sortBy('name')
            .value();

        vm.noteTypesById = _.keyBy(vm.allNoteTypes, 'id');

        const noteTitles = _.chain(vm.notes)
            .map(n => vm.noteTypesById[n.namedNoteTypeId])
            .map(nt => nt.name).value();

        vm.labelText = vm.baseLabelText + ': ' + _.truncate(_.join(noteTitles, ', '), {length: 200});

    };

    vm.cancelNewNote = () => {
        invokeFunction(vm.onEditorDismiss);
    };

    vm.saveNewNote = () => {
        invokeFunction(vm.onSave, vm.newNote)
            .then(() => {
                vm.newNote = {};
                invokeFunction(vm.onEditorDismiss);
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

        invokeFunction(vm.onSave, newNote)
            .then(() => {
                vm.editingNotes[note.namedNoteTypeId] = null;
            });
    };

    vm.deleteNote = (note) => {
        if (confirm('Are you sure you want to delete this note?')) {
            invokeFunction(vm.onDelete, note);
        }
    };

    vm.expandNote = (note) => vm.expandedNotes[note.namedNoteTypeId] = true;

    vm.collapseNote = (note) => vm.expandedNotes[note.namedNoteTypeId] = false;
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzEntityNamedNotesPanel'
};