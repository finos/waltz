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
import {initialiseData, invokeFunction} from "../../common";

const template = require('./entity-named-notes-panel.html');


const bindings = {
    notes: '<',
    allNoteTypes: '<',
    parentEntityRef: '<',
    editRole: '@',
    onCreate: '<',
    onEdit: '<',
    onDelete: '<'
};


const initialState = {
    creatingNote: false,
    creatableNoteTypes: [],
    expandedNotes: {},
    newNote: {},
    baseLabelText: 'Show Additional Notes',
    labelText: 'Show Additional Notes',
    visibility: {
        notes: false
    }
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.notes && vm.allNoteTypes && vm.parentEntityRef) {
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
                    .map(nt => nt.name)
                    .value();

                vm.labelText = vm.baseLabelText + ': ' + _.truncate(_.join(noteTitles, ', '), { length: 200 });
            }
        }
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
        invokeFunction(vm.onCreate, Object.assign({}, vm.newNote));
        vm.newNote = {};
        vm.creatingNote = false;
        vm.visibility.notes = true;
    };

    vm.updateNote =
        (noteTypeId, change) => invokeFunction(vm.onEdit, {
            namedNoteTypeId: noteTypeId,
            noteText: change.newVal
        });

    vm.deleteNote = (note) => {
        if (confirm('Are you sure you want to delete this note?')) {
            invokeFunction(vm.onDelete, note);
        }
    };

    vm.expandNote =
        (note) => vm.expandedNotes[note.namedNoteTypeId] = true;

    vm.collapseNote =
        (note) => vm.expandedNotes[note.namedNoteTypeId] = false;
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;