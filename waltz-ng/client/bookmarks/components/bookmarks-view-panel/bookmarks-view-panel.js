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
import template from './bookmarks-view-panel.html';
import {initialiseData} from "../../../common/index";


const bindings = {
    bookmarks: '<',
    parentEntityRef: '<'
};


const initialState = {
    bookmarkKind: null,
    filteredBookmarks: []
};


function filterBookmarks(bookmarks = [], kind = null) {
    if (_.isEmpty(bookmarks)) return [];

    const byKind = _.groupBy(bookmarks, 'bookmarkKind');

    const groupsToShow = kind
        ? { kind : byKind[kind] }
        : byKind;

    return _.map(groupsToShow, (v, k) => ({ kind: k, bookmarks: v }));
}


function controller() {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.filteredBookmarks = filterBookmarks(vm.bookmarks, vm.bookmarkKind);
    };

    vm.$onChanges = () => {
        refresh();
    };

    vm.selectBookmarkKind = (kind) => {
        vm.bookmarkKind = kind;
        refresh();
    };
}


const component = {
    controller,
    template,
    bindings
};


export default {
    id: 'waltzBookmarksViewPanel',
    component
};