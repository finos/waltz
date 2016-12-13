/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common";
import {bookmarkNames} from "../../../common/services/display_names";
import {bookmarkIconNames} from "../../../common/services/icon_names";


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


function createKinds(bookmarks = []) {
    const bookmarksByKind = _.groupBy(bookmarks, 'kind');

    return _.chain(_.keys(bookmarkNames))
        .union(_.keys(bookmarkIconNames))
        .sortBy()
        .map(k => ({
            code: k,
            name: bookmarkNames[k] || '?',
            icon: bookmarkIconNames[k] || 'square-o',
            count: bookmarksByKind[k] ? bookmarksByKind[k].length : 0,
            selected: false
        }))
        .value();
}


const template = require('./bookmark-kinds.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.kinds = createKinds(vm.bookmarks);
    };


    const resetCurrentSelection = () => {
        if(vm.currentSelection) vm.currentSelection.selected = false;
        vm.currentSelection = null;
    };


    vm.select = (kind) => {
        if(!kind.count) return;

        if(vm.currentSelection != kind) resetCurrentSelection();

        kind.selected = !kind.selected;
        invokeFunction(vm.onSelect, kind.selected ? kind.code : null);

        vm.currentSelection = kind;
    };


    vm.clearSelection = () => {
        invokeFunction(vm.onSelect, null);
        resetCurrentSelection();
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;