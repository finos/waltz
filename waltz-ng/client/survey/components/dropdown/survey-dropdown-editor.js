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
import {initialiseData, invokeFunction} from "../../../common";
import template from "./survey-dropdown-editor.html";


const bindings = {
    entries: "<",
    onChange: "<"
};


const initialState = {
    creatingEntry: false,
    entries: [],
    bulkEntriesString: null,
    editor: "TABULAR",
    newEntry: null,
    onSave: () => console.log("default onSave ")
};


function createEntriesString(entries = []) {
    return _.chain(entries)
        .map("value")
        .join("\n");
}


function mkEntry(id, position, value) {
    return {
        id,
        value,
        position
    };
}


function controller(notification, $timeout) {
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
        $timeout(() => document.getElementById("wsde-entry-input").focus());

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

    vm.updateValue = (data) => {
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
        vm.editor = "TABULAR"
    };

    vm.showBulkEditor = () => {
        vm.bulkEntriesString = createEntriesString(vm.entries);
        vm.editor = "BULK";
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
    "Notification",
    "$timeout"
];


const component = {
    template,
    bindings,
    controller
};


export default component;