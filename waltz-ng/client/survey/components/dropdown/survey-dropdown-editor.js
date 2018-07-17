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

import _ from 'lodash';
import {initialiseData, invokeFunction} from '../../../common';
import template from './survey-dropdown-editor.html';


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


function createEntriesString(entries = []) {
    return _.chain(entries)
        .map('value')
        .join("\n");
}


function mkEntry(id, position, value) {
    return {
        id,
        value,
        position
    };
}


function controller(notification) {
    const vm = this;

    vm.$onInit = () => { initialiseData(vm, initialState); };

    const notifyChanges = () => {
        invokeFunction(vm.onChange, vm.entries);
    };

    const valueExists = (value) => {
        return _.find(vm.entries, e => e.value === value) !== undefined;
    };

    vm.startNewEntry = () => {
        vm.creatingEntry = true;
    };

    vm.saveNewEntry = (entry) => {
        const newEntry = mkEntry(null, null, entry.value);

        if (!valueExists(newEntry.value)) {
            vm.entries = _.concat(vm.entries, [entry]);
        } else {
            notification.warning(`\'${entry.value}\' already exists, will not add`)
        }
        vm.newEntry = null;
        vm.creatingEntry = false;
        notifyChanges();
    };

    vm.cancelNewEntry = () => {
        vm.creatingEntry = false;
    };

    vm.updateValue = (entryId, data) => {
        //entry id is likely to be undefined and cannot be relied upon (when we have new entries)
        if (!valueExists(data.newVal)) {
            vm.entries = _.map(vm.entries, e => {
                if(e.value === data.oldVal) {
                    return mkEntry(e.id, e.position, data.newVal);
                } else {
                    return e;
                }
            });
        } else {
            notification.warning(`\'${data.newVal}\' already exists, will not add`)
        }


        notifyChanges();
    };

    vm.removeEntry = (entry) => {
        vm.entries = _.reject(vm.entries, e => e.value === entry.value);
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
        vm.entries = _.chain(vm.bulkEntriesString)
            .trim()
            .split("\n")
            .uniq()
            .map(s => mkEntry(null, null, s))
            .value();
        notifyChanges();
    };
}


controller.$inject = [
    'Notification'
];


const component = {
    template,
    bindings,
    controller
};


export default component;