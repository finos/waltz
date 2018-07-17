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