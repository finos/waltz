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
import template from "./bookmarks-edit-panel.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    bookmarks: "<",
    onDismiss: "<",
    onReload: "<",
    parentEntityRef: "<"
};


const initialState = {
    showFilter: false,
    visibility: {
        form: false
    },
    selectedBookmark: null
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.edit = (b) => {
        vm.selectedBookmark = Object.assign({}, b);
        vm.visibility.form = true;
    };

    vm.create = () => {
        vm.selectedBookmark = {
            bookmarkKind: "DOCUMENTATION",
            lastUpdatedBy: "ignored, server will set"
        };
        vm.visibility.form = true;
    };

    vm.onSave = (b) => {
        b.parent = vm.parentEntityRef;
        serviceBroker
            .execute(CORE_API.BookmarkStore.save, [b])
            .then(() => {
                vm.onReload();
                vm.resetForm();
                notification.success("Updated bookmarks")
            });
    };

    vm.onCancel = () => {
        vm.visibility.form = false;
    };

    vm.resetForm = () => {
        vm.visibility.form = false;
        vm.bookmark = {
            bookmarkKind: "DOCUMENTATION",
            parent: vm.parentEntityRef
        };
    };

    vm.remove = (b) => {
        vm.visibility.form = false;
        if (confirm("Are you sure you want to remove this bookmark ?")) {
            serviceBroker
                .execute(CORE_API.BookmarkStore.remove, [b.id])
                .then(() => {
                    vm.onReload();
                    notification.warning("Removed bookmark");
                });
        }
    };

}


controller.$inject = ["Notification", "ServiceBroker"];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzBookmarksEditPanel",
    component
};