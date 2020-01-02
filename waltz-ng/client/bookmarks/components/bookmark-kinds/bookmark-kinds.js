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
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './bookmark-kinds.html';


const bindings = {
    bookmarks: '<',
    onSelect: '<'
};


const initialState = {
    bookmarks: [],
    kinds: [],
    currentSelection: null,
    onSelect: (kind) => 'no onSelect handler defined for bookmark-kinds: ' + kind
};


function createKinds(serviceBroker, bookmarks = []) {
    const bookmarksByKind = _.groupBy(bookmarks, 'bookmarkKind');

    return serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => {
            const enumValues = r.data;
            return _
                .chain(enumValues)
                .filter({ type: 'BookmarkKind' })
                .sortBy('name')
                .map(k => ({
                    code: k.key,
                    name: k.name,
                    icon: k.icon,
                    description: k.description,
                    count: (bookmarksByKind[k.key] || []).length,
                    selected: false
                }))
                .value();
        });
}




function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        createKinds(serviceBroker, vm.bookmarks)
            .then(kinds => vm.kinds = kinds);
    };

    const resetCurrentSelection = () => {
        if(vm.currentSelection) {
            vm.currentSelection.selected = false;
        }
        vm.currentSelection = null;
    };

    vm.select = (kind) => {
        if (!kind.count) {
            return;
        }

        if (vm.currentSelection != kind) {
            resetCurrentSelection();
        }

        kind.selected = !kind.selected;
        invokeFunction(vm.onSelect, kind.selected ? kind.code : null);

        vm.currentSelection = kind;
    };

    vm.clearSelection = () => {
        invokeFunction(vm.onSelect, null);
        resetCurrentSelection();
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
    id: 'waltzBookmarkKinds'
};