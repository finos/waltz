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

import _ from 'lodash';
import {initialiseData, invokeFunction} from '../../../common';


const bindings = {
    entries: '<',
    onChange: '<'
};


const initialState = {
    creatingEntry: false,
    entries: [],
    bulkEntriesString: null,
    editor: 'TABULAR',
    newEntry: null,
    onSave: () => console.log("default onSave ")
};


const template = require('./survey-dropdown-editor.html');


function createEntriesString(entries = []) {
    return _.chain(entries)
        .map('value')
        .join("\n");
}


function controller() {
    const vm = this;

    vm.$onInit = () => {
        initialiseData(vm, initialState);
        vm.bulkEntriesString = createEntriesString(vm.entries);
    };

    const notifyChanges = () => {
        invokeFunction(vm.onChange, vm.entries);
    };

    vm.startNewEntry = () => {
        vm.creatingEntry = true;
    };

    vm.saveNewEntry = (entry) => {
        vm.entries.push(entry);
        vm.newEntry = null;
        vm.creatingEntry = false;
        notifyChanges();
    };

    vm.cancelNewEntry = () => {
        vm.creatingEntry = false;
    };

    vm.updateValue = (entryId, data) => {
        //entry id is likely to be undefined and cannot be relied upon (when we have new entries)
        const entry = _.find(vm.entries, e => e.value === data.oldVal);
        entry.value = data.newVal;
        notifyChanges();
    };

    vm.removeEntry = (entry) => {
        _.remove(vm.entries, e => e.value === entry.value);
        notifyChanges();
    };

    vm.showTabularEditor = () => {
        vm.editor = 'TABULAR'
    };

    vm.showBulkEditor = () => {
        vm.bulkEntriesString = createEntriesString(vm.entries);
        vm.editor = 'BULK';
    };

    vm.bulkEntriesChanged = () => {
        const newEntries = _.chain(vm.bulkEntriesString)
            .trim()
            .split("\n")
            .uniq()
            .map(s => ({
                id: null,
                value: s,
                position: null
            }))
            .value();
        vm.entries = newEntries;
        notifyChanges();
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;