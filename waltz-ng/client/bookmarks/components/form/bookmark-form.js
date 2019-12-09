
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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