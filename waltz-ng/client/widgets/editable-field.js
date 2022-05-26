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
import {formats, initialiseData, invokeFunction} from "../common";

import template from "./editable-field.html";
import moment from "moment";


const bindings = {
    initialVal: "<?",
    onSave: "<",  // e.g.: (d, ctx) => console.log(d.newVal, d.oldVal, ctx)
    fieldType: "@",  // logical-data-element | person | text | textarea | boolean | date | markdown | number
    dateFormat: "@?",
    ctx: "<?",
    buttonLabel: "@",
    saveLabel: "@?",
    editRole: "@",
    emptyLabel: "@?",
    startInEditMode: "<?",
    onCancel: "<?",
    maxDate: "<?",  // only for date fields
    minDate: "<?",  // only for date fields
    inlineHelp: "@?",
    readOnly: "<?"
};


const initialState = {
    initialVal: null,
    dateFormat: "yyyy-MM-dd",
    ctx: null,
    errorMessage: "",
    emptyLabel: null,
    editing: false,
    saving: false,
    fieldType: "text",
    buttonLabel: "Edit",
    saveLabel:  "Save",
    startInEditMode: false,
    onCancel: null,
    onSave: () => console.log("WEF: No on-save method provided"),
    readOnly: false
};


function mkNewVal(initialVal, fieldType) {
    return initialVal && fieldType === "date" ?
        moment(initialVal, formats.parseDateOnly).toDate()
        : initialVal;
}


function controller($element, $timeout) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        if (vm.startInEditMode) {
            vm.editing = true;
        }
    };

    vm.$onChanges = (c) => {
        if (c.initialVal) {
            vm.newVal = mkNewVal(vm.initialVal, vm.fieldType);
        }
    };

    const saveComplete = () => {
        vm.saving = false;
        vm.editing = false;
        vm.errorMessage = "";
    };

    const saveFailed = (e) => {
        vm.saving = false;
        vm.editing = true;
        vm.errorMessage = e;
    };

    vm.save = () => {
        const data = {
            newVal: vm.newVal,
            oldVal: vm.initialVal
        };

        vm.saving = true;

        const promise = vm.onSave(data, vm.ctx);

        if (promise) {
            promise
                .then(saveComplete, saveFailed)
                .then(() => vm.initialVal = data.newVal);
        } else {
            saveComplete();
        }
    };

    vm.edit = () => {
        vm.editing = true;
        vm.newVal = mkNewVal(vm.initialVal, vm.fieldType);
        const tagType = vm.fieldType === "textarea" || vm.fieldType === "markdown"
            ? "textarea"
            : "input";
        $timeout(() => $element.find(tagType)[0]?.focus());
    };

    vm.cancel = () => {
        vm.editing = false;
        vm.saving = false;
        vm.errorMessage = "";
        invokeFunction(vm.onCancel);
    };

    vm.entitySelect = (entity) => {
        vm.newVal = entity;
    };

    vm.onKeyDown = (event) => {
        if (event.ctrlKey && event.keyCode === 13) {  // ctrl + enter
            vm.save();
        }
    };

}


controller.$inject = ["$element", "$timeout"];


const component = {
    template,
    bindings,
    controller,
    transclude: {
        inlineHelp: "?inlineHelp"
    }
};


export default component;