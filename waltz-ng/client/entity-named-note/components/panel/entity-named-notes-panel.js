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

import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common/index";

import template from "./entity-named-notes-panel.html";


const bindings = {
    parentEntityRef: "<",
    notes: "<",
    allNoteTypes: "<",
    onSave: "<",
    onDelete: "<",
    onEditorDismiss: "<",
    creatingNote: "<"
};


const initialState = {
    creatableNoteTypes: [],
    editingNotes: {},
    expandedNotes: {},
    newNote: {},
    baseLabelText: "Show Additional Notes",
    labelText: "Show Additional Notes",
    visibility: {notes: true},
    onSave: (note) => console.log("entity named notes panel default save handler: ", note),
    onDelete: (note) => console.log("entity named notes panel default delete handler: ", note),
    onEditorDismiss: () => console.log("entity named notes panel default dismiss editor handler: ", note),
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes) => {
        if(vm.notes && vm.allNoteTypes) {
            postLoad();
        }
    };


    const postLoad = () => {
        const existingNotesByTypeId = _.keyBy(vm.notes, "namedNoteTypeId");

        vm.creatableNoteTypes = _.chain(vm.allNoteTypes)
            .filter(nt => !nt.isReadOnly)
            .filter(nt => !existingNotesByTypeId[nt.id])
            .filter(nt => nt.applicableEntityKinds.indexOf(vm.parentEntityRef.kind) !== -1)
            .sortBy("name")
            .value();

        vm.noteTypesById = _.keyBy(vm.allNoteTypes, "id");

        vm.notes = _.sortBy(vm.notes,
                            n => [vm.noteTypesById[n.namedNoteTypeId].position,
                                    vm.noteTypesById[n.namedNoteTypeId].id]);

        const noteTitles = _.chain(vm.notes)
            .map(n => vm.noteTypesById[n.namedNoteTypeId])
            .map(nt => nt.name).value();

        vm.labelText = vm.baseLabelText + ": " + _.truncate(_.join(noteTitles, ", "), {length: 200});

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
        if (confirm("Are you sure you want to delete this note?")) {
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
    id: "waltzEntityNamedNotesPanel"
};