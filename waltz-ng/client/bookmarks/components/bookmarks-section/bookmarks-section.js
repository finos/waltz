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

import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from "../../../common";

import template from './bookmarks-section.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    visibility: {
        editor: false
    }
};



function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const load = (force = false) => {
        serviceBroker
            .loadViewData(
                CORE_API.BookmarkStore.findByParent,
                [ vm.parentEntityRef ],
                { force })
            .then(r => vm.bookmarks = r.data);
    };

    vm.$onInit = () => {
        if(vm.parentEntityRef) {
            load();
        }
    };

    vm.onReload = () => {
        load(true);
    };

    vm.onDismiss = () => {
        vm.visibility.editor = false;
    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzBookmarksSection'
};