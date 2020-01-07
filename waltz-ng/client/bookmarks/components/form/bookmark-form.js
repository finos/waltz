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

import {initialiseData, isEmpty} from "../../../common";
import template from "./bookmark-form.html";


const bindings = {
    bookmark: "<",
    onSubmit: "<",
    onCancel: "<",
    confirmLabel: "@?"
};


const initialState = {
    confirmLabel: "Save",
    submitDisabled: true
};


function controller() {

    const vm = initialiseData(this, initialState);

    vm.onKindSelect = (code) => {
        vm.bookmark.bookmarkKind = code;
    };

    vm.togglePrimary = () => {
        vm.bookmark.isPrimary = !vm.bookmark.isPrimary;
    };

    vm.toggleRestricted = () => {
        vm.bookmark.isRestricted = !vm.bookmark.isRestricted;
    };

    vm.onFormChange = () => {
        const { url } = vm.bookmark;
        vm.submitDisabled = isEmpty(url);
    };

    vm.$onChanges = () => {
        vm.onFormChange();
    };
}

controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzBookmarkForm"
}